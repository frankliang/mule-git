/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jms;

import org.mule.api.MessagingException;
import org.mule.api.MuleException;
import org.mule.api.context.notification.ConnectionNotificationListener;
import org.mule.api.context.notification.ServerNotification;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.StartException;
import org.mule.api.service.Service;
import org.mule.api.transaction.Transaction;
import org.mule.api.transaction.TransactionException;
import org.mule.api.transport.MessageAdapter;
import org.mule.api.transport.MessageReceiver;
import org.mule.api.transport.ReplyToHandler;
import org.mule.config.ExceptionHelper;
import org.mule.config.i18n.CoreMessages;
import org.mule.context.notification.ConnectionNotification;
import org.mule.context.notification.NotificationException;
import org.mule.transaction.TransactionCoordination;
import org.mule.transport.AbstractConnector;
import org.mule.transport.ConnectException;
import org.mule.transport.FatalConnectException;
import org.mule.transport.jms.i18n.JmsMessages;
import org.mule.transport.jms.xa.ConnectionFactoryWrapper;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.XAConnectionFactory;
import javax.naming.NamingException;

import org.apache.commons.lang.UnhandledException;

/**
 * <code>JmsConnector</code> is a JMS 1.0.2b compliant connector that can be used
 * by a Mule endpoint. The connector supports all JMS functionality including topics
 * and queues, durable subscribers, acknowledgement modes and local transactions.
 */

public class JmsConnector extends AbstractConnector implements ConnectionNotificationListener
{

    public static final String JMS = "jms";

    ////////////////////////////////////////////////////////////////////////
    // Properties
    ////////////////////////////////////////////////////////////////////////

    private int acknowledgementMode = Session.AUTO_ACKNOWLEDGE;

    private String clientId;

    private boolean durable;

    private boolean noLocal;

    private boolean persistentDelivery;

    private boolean honorQosHeaders;

    private int maxRedelivery = 0;

    private boolean cacheJmsSessions = false;

    private boolean recoverJmsConnections = true;

    /** Whether to create a consumer on connect. */
    private boolean eagerConsumer = true;

    ////////////////////////////////////////////////////////////////////////
    // JMS Connection
    ////////////////////////////////////////////////////////////////////////

    private ConnectionFactory connectionFactory;

    public String username = null;

    public String password = null;

    /**
     * JMS Connection, not settable by the user.
     */
    private Connection connection;

    ////////////////////////////////////////////////////////////////////////
    // Strategy classes
    ////////////////////////////////////////////////////////////////////////

    private String specification = JmsConstants.JMS_SPECIFICATION_102B;

    private JmsSupport jmsSupport;

    private JmsTopicResolver topicResolver;

    private RedeliveryHandlerFactory redeliveryHandlerFactory;

    ////////////////////////////////////////////////////////////////////////
    // Methods
    ////////////////////////////////////////////////////////////////////////

    /* Register the Jms Exception reader if this class gets loaded */
    static
    {
        ExceptionHelper.registerExceptionReader(new JmsExceptionReader());
    }

    public String getProtocol()
    {
        return JMS;
    }

    protected void doInitialise() throws InitialisationException
    {
        if (connectionFactory == null)
        {
            connectionFactory = getDefaultConnectionFactory();
        }
        if (connectionFactory == null)
        {
            throw new InitialisationException(JmsMessages.noConnectionFactorySet(), this);
        }

        if (topicResolver == null)
        {
            topicResolver = new DefaultJmsTopicResolver(this);
        }
        if (redeliveryHandlerFactory == null)
        {
            redeliveryHandlerFactory = new DefaultRedeliveryHandlerFactory();
        }

        try
        {
            muleContext.registerListener(this, getName());
        }
        catch (NotificationException nex)
        {
            throw new InitialisationException(nex, this);
        }
    }

    /** Override this method to provide a default ConnectionFactory for a vendor-specific JMS Connector. */
    protected ConnectionFactory getDefaultConnectionFactory()
    {
        return null;
    }

    protected void doDispose()
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (JMSException e)
            {
                logger.error("Jms connector failed to dispose properly: ", e);
            }
            connection = null;
        }
    }

    protected Connection createConnection() throws NamingException, JMSException, InitialisationException
    {
        ConnectionFactory cf = this.connectionFactory;
        Connection connection;

        try
        {
            if (cf instanceof XAConnectionFactory && muleContext.getTransactionManager() != null)
            {
                cf = new ConnectionFactoryWrapper(cf);
            }
        }
        catch (Exception e)
        {
            throw new InitialisationException(e, this);
        }
        if (cf == null)
        {
            throw new InitialisationException(JmsMessages.noConnectionFactorySet(), this);
        }


        if (username != null)
        {
            connection = jmsSupport.createConnection(cf, username, password);
        }
        else
        {
            connection = jmsSupport.createConnection(cf);
        }

        if (clientId != null)
        {
            connection.setClientID(getClientId());
        }

        // Register a JMS exception listener to detect failed connections.
        // Existing connection strategy will be used to recover.

        if (recoverJmsConnections && connectionStrategy != null && connection != null)
        {
            connection.setExceptionListener(new ExceptionListener()
            {
                public void onException(JMSException jmsException)
                {
                    logger.debug("About to recycle myself due to remote JMS connection shutdown.");
                    final JmsConnector jmsConnector = JmsConnector.this;
                    try
                    {
                        jmsConnector.stop();
                        jmsConnector.initialised.set(false);
                    }
                    catch (MuleException e)
                    {
                        logger.warn(e.getMessage(), e);
                    }

                    try
                    {
                        connectionStrategy.connect(jmsConnector);

                        // TODO The following code fragment until jmsConnector.start() is a workaround as 
                        // suggested in MULE-1720. The real solution will be the long awaited re-connection
                        // strategy implementation.

                        // keep the receivers in memory so we can register them after initialization
                        Map receivers = new HashMap(jmsConnector.getReceivers());
                        jmsConnector.initialise();
                        // register the receivers
                        for (Iterator itReceivers = receivers.values().iterator(); itReceivers.hasNext();) 
                        {
                            MessageReceiver receiver = (MessageReceiver) itReceivers.next();
                            try 
                            {
                                jmsConnector.registerListener(receiver.getService(), receiver.getEndpoint());
                            } 
                            catch (Exception ex) 
                            {
                                throw new FatalConnectException(ex, receiver);
                            }
                        }
                        
                        jmsConnector.start();
                    }
                    catch (FatalConnectException fcex)
                    {
                        logger.fatal("Failed to reconnect to JMS server. I'm giving up.");
                    }
                    catch (MuleException umoex)
                    {
                        throw new UnhandledException("Failed to recover a connector.", umoex);
                    }
                }
            });
        }

        return connection;
    }

    protected void doConnect() throws ConnectException
    {
        try
        {
            if (jmsSupport == null)
            {
                if (JmsConstants.JMS_SPECIFICATION_102B.equals(specification))
                {
                    jmsSupport = new Jms102bSupport(this);
                }
                else
                {
                    jmsSupport = new Jms11Support(this);
                }
            }
        }
        catch (Exception e)
        {
            throw new ConnectException(CoreMessages.failedToCreate("Jms Connector"), e, this);
        }

        try
        {
            connection = createConnection();
            if (started.get())
            {
                connection.start();
            }
        }
        catch (Exception e)
        {
            throw new ConnectException(e, this);
        }
    }

    protected void doDisconnect() throws ConnectException
    {
        try
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (Exception e)
        {
            throw new ConnectException(e, this);
        }
        finally
        {
            // connectionFactory = null;
            connection = null;
        }
    }

    public MessageAdapter getMessageAdapter(Object message) throws MessagingException
    {
        JmsMessageAdapter adapter = (JmsMessageAdapter) super.getMessageAdapter(message);
        adapter.setSpecification(this.getSpecification());
        return adapter;
    }

    protected Object getReceiverKey(Service service, InboundEndpoint endpoint)
    {
        return service.getName() + "~" + endpoint.getEndpointURI().getAddress();
    }

    public Session getSessionFromTransaction()
    {
        Transaction tx = TransactionCoordination.getInstance().getTransaction();
        if (tx != null)
        {
            if (tx.hasResource(connection))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Retrieving jms session from current transaction " + tx);
                }

                Session session = (Session) tx.getResource(connection);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Using " + session + " bound to transaction " + tx);
                }

                return session;
            }
        }
        return null;
    }

    public Session getSession(ImmutableEndpoint endpoint) throws JMSException
    {
        final boolean topic = getTopicResolver().isTopic(endpoint);
        return getSession(endpoint.getTransactionConfig().isTransacted(), topic);
    }

    public Session getSession(boolean transacted, boolean topic) throws JMSException
    {
        if (!isConnected())
        {
            throw new JMSException("Not connected");
        }
        Session session = getSessionFromTransaction();
        if (session != null)
        {
            return session;
        }

        Transaction tx = TransactionCoordination.getInstance().getTransaction();

        if (logger.isDebugEnabled())
        {
            logger.debug(MessageFormat.format(
                    "Retrieving new jms session from connection: " +
                            "topic={0}, transacted={1}, ack mode={2}, nolocal={3}",
                    new Object[]{Boolean.valueOf(topic),
                                 Boolean.valueOf(transacted),
                                 new Integer(acknowledgementMode),
                                 Boolean.valueOf(noLocal)}));
        }

        session = jmsSupport.createSession(connection, topic, transacted, acknowledgementMode, noLocal);
        if (tx != null)
        {
            logger.debug("Binding session " + session + " to current transaction " + tx);
            try
            {
                tx.bindResource(connection, session);
            }
            catch (TransactionException e)
            {
                closeQuietly(session);
                throw new RuntimeException("Could not bind session to current transaction", e);
            }
        }
        return session;
    }

    protected void doStart() throws MuleException
    {
        if (connection != null)
        {
            try
            {
                connection.start();
            }
            catch (JMSException e)
            {
                throw new StartException(CoreMessages.failedToStart("Jms Connection"), e, this);
            }
        }
    }

    protected void doStop() throws MuleException
    {
        // template method
    }

    public ReplyToHandler getReplyToHandler()
    {
        return new JmsReplyToHandler(this, getDefaultResponseTransformers());
    }

    public void onNotification(ServerNotification notification)
    {
        if (notification.getAction() == ConnectionNotification.CONNECTION_DISCONNECTED
                || notification.getAction() == ConnectionNotification.CONNECTION_FAILED)
        {
            // Remove all dispatchers as any cached session will be invalidated
            disposeDispatchers();
            // TODO should we dispose receivers here as well (in case they are
            // transactional)
            // gives a harmless NPE at
            // AbstractConnector.connect(AbstractConnector.java:927)
            // disposeReceivers();
        }
    }

    /**
     * This method may be overridden in case a certain JMS implementation does not
     * support all the standard JMS properties.
     */
    public boolean supportsProperty(String property)
    {
        return true;
    }

    /**
     * This method may be overridden in order to apply pre-processing to the message
     * as soon as it arrives.
     *
     * @param message - the incoming message
     * @param session - the JMS session
     * @return the preprocessed message
     */
    public javax.jms.Message preProcessMessage(javax.jms.Message message, Session session) throws Exception
    {
        return message;
    }

    /**
     * Closes the MessageProducer
     *
     * @param producer
     * @throws JMSException
     */
    public void close(MessageProducer producer) throws JMSException
    {
        if (producer != null)
        {
            producer.close();
        }
    }

    /**
     * Closes the MessageProducer without throwing an exception (an error message is
     * logged instead).
     *
     * @param producer
     */
    public void closeQuietly(MessageProducer producer)
    {
        try
        {
            close(producer);
        }
        catch (JMSException e)
        {
            logger.error("Failed to close jms message producer", e);
        }
    }

    /**
     * Closes the MessageConsumer
     *
     * @param consumer
     * @throws JMSException
     */
    public void close(MessageConsumer consumer) throws JMSException
    {
        if (consumer != null)
        {
            consumer.close();
        }
    }

    /**
     * Closes the MessageConsumer without throwing an exception (an error message is
     * logged instead).
     *
     * @param consumer
     */
    public void closeQuietly(MessageConsumer consumer)
    {
        try
        {
            close(consumer);
        }
        catch (JMSException e)
        {
            logger.error("Failed to close jms message consumer", e);
        }
    }

    /**
     * Closes the MuleSession
     *
     * @param session
     * @throws JMSException
     */
    public void close(Session session) throws JMSException
    {
        if (session != null)
        {
            session.close();
        }
    }

    /**
     * Closes the MuleSession without throwing an exception (an error message is logged
     * instead).
     *
     * @param session
     */
    public void closeQuietly(Session session)
    {
        try
        {
            close(session);
        }
        catch (JMSException e)
        {
            logger.error("Failed to close jms session consumer", e);
        }
    }

    /**
     * Closes the TemporaryQueue
     *
     * @param tempQueue
     * @throws JMSException
     */
    public void close(TemporaryQueue tempQueue) throws JMSException
    {
        if (tempQueue != null)
        {
            tempQueue.delete();
        }
    }

    /**
     * Closes the TemporaryQueue without throwing an exception (an error message is
     * logged instead).
     *
     * @param tempQueue
     */
    public void closeQuietly(TemporaryQueue tempQueue)
    {
        try
        {
            close(tempQueue);
        }
        catch (JMSException e)
        {
            if (logger.isErrorEnabled())
            {
                String queueName = "";
                try
                {
                    queueName = tempQueue.getQueueName();
                }
                catch (JMSException innerEx)
                {
                    // ignore, we are just trying to get the queue name
                }
                logger.info(MessageFormat.format(
                        "Faled to delete a temporary queue '{0}' Reason: {1}",
                        new Object[]{queueName, e.getMessage()}));
            }
        }
    }

    /**
     * Closes the TemporaryTopic
     *
     * @param tempTopic
     * @throws JMSException
     */
    public void close(TemporaryTopic tempTopic) throws JMSException
    {
        if (tempTopic != null)
        {
            tempTopic.delete();
        }
    }

    /**
     * Closes the TemporaryTopic without throwing an exception (an error message is
     * logged instead).
     *
     * @param tempTopic
     */
    public void closeQuietly(TemporaryTopic tempTopic)
    {
        try
        {
            close(tempTopic);
        }
        catch (JMSException e)
        {
            if (logger.isErrorEnabled())
            {
                String topicName = "";
                try
                {
                    topicName = tempTopic.getTopicName();
                }
                catch (JMSException innerEx)
                {
                    // ignore, we are just trying to get the topic name
                }
                logger.error("Faled to delete a temporary topic " + topicName, e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ////////////////////////////////////////////////////////////////////////

    /** @return Returns the connection. */
    public Connection getConnection()
    {
        return connection;
    }

    protected void setConnection(Connection connection)
    {
        this.connection = connection;
    }

    /** @return Returns the acknowledgeMode. */
    public int getAcknowledgementMode()
    {
        return acknowledgementMode;
    }

    /** @param acknowledgementMode The acknowledgementMode to set. */
    public void setAcknowledgementMode(int acknowledgementMode)
    {
        this.acknowledgementMode = acknowledgementMode;
    }

    /** @return Returns the durable. */
    public boolean isDurable()
    {
        return durable;
    }

    /** @param durable The durable to set. */
    public void setDurable(boolean durable)
    {
        this.durable = durable;
    }

    /** @return Returns the noLocal. */
    public boolean isNoLocal()
    {
        return noLocal;
    }

    /** @param noLocal The noLocal to set. */
    public void setNoLocal(boolean noLocal)
    {
        this.noLocal = noLocal;
    }

    /** @return Returns the persistentDelivery. */
    public boolean isPersistentDelivery()
    {
        return persistentDelivery;
    }

    /** @param persistentDelivery The persistentDelivery to set. */
    public void setPersistentDelivery(boolean persistentDelivery)
    {
        this.persistentDelivery = persistentDelivery;
    }

    public JmsSupport getJmsSupport()
    {
        return jmsSupport;
    }

    public void setJmsSupport(JmsSupport jmsSupport)
    {
        this.jmsSupport = jmsSupport;
    }

    public String getSpecification()
    {
        return specification;
    }

    public void setSpecification(String specification)
    {
        this.specification = specification;
    }

    public void setRecoverJmsConnections(boolean recover)
    {
        this.recoverJmsConnections = recover;
    }

    public boolean isRecoverJmsConnections()
    {
        return this.recoverJmsConnections;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    public int getMaxRedelivery()
    {
        return maxRedelivery;
    }

    public void setMaxRedelivery(int maxRedelivery)
    {
        this.maxRedelivery = maxRedelivery;
    }

    public boolean isRemoteSyncEnabled()
    {
        return true;
    }


    /**
     * Getter for property 'topicResolver'.
     *
     * @return Value for property 'topicResolver'.
     */
    public JmsTopicResolver getTopicResolver()
    {
        return topicResolver;
    }

    /**
     * Setter for property 'topicResolver'.
     *
     * @param topicResolver Value to set for property 'topicResolver'.
     */
    public void setTopicResolver(final JmsTopicResolver topicResolver)
    {
        this.topicResolver = topicResolver;
    }

    /**
     * Getter for property 'eagerConsumer'. Default
     * is {@code true}.
     *
     * @return Value for property 'eagerConsumer'.
     * @see #eagerConsumer
     */
    public boolean isEagerConsumer()
    {
        return eagerConsumer;
    }

    /**
     * A value of {@code true} will create a consumer on
     * connect, in contrast to lazy instantiation in the poll loop.
     * This setting very much depends on the JMS vendor.
     * Affects transactional receivers, typical symptoms are:
     * <ul>
     * <li> consumer thread hanging forever, though a message is
     * available
     * <li>failure to consume the first message (the rest
     * are fine)
     * </ul>
     * <p/>
     *
     * @param eagerConsumer Value to set for property 'eagerConsumer'.
     * @see #eagerConsumer
     * @see org.mule.transport.jms.XaTransactedJmsMessageReceiver
     */
    public void setEagerConsumer(final boolean eagerConsumer)
    {
        this.eagerConsumer = eagerConsumer;
    }

    public boolean isCacheJmsSessions()
    {
        return cacheJmsSessions;
    }

    public void setCacheJmsSessions(boolean cacheJmsSessions)
    {
        this.cacheJmsSessions = cacheJmsSessions;
    }

    public ConnectionFactory getConnectionFactory()
    {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }
    
    public RedeliveryHandlerFactory getRedeliveryHandlerFactory()
    {
        return redeliveryHandlerFactory;
    }
    
    public void setRedeliveryHandlerFactory(RedeliveryHandlerFactory redeliveryHandlerFactory)
    {
        this.redeliveryHandlerFactory = redeliveryHandlerFactory;
    }

    /**
     * Sets the <code>honorQosHeaders</code> property, which determines whether
     * {@link JmsMessageDispatcher} should honor incoming message's QoS headers
     * (JMSPriority, JMSDeliveryMode).
     * 
     * @param honorQosHeaders <code>true</code> if {@link JmsMessageDispatcher}
     *            should honor incoming message's QoS headers; otherwise
     *            <code>false</code> Default is <code>false</code>, meaning that
     *            connector settings will override message headers.
     */
   public void setHonorQosHeaders(boolean honorQosHeaders)
   {
       this.honorQosHeaders = honorQosHeaders;
   }

   /**
     * Gets the value of <code>honorQosHeaders</code> property.
     * 
     * @return <code>true</code> if <code>JmsMessageDispatcher</code> should
     *         honor incoming message's QoS headers; otherwise <code>false</code>
     *         Default is <code>false</code>, meaning that connector settings will
     *         override message headers.
     */
   public boolean isHonorQosHeaders()
   {
       return honorQosHeaders;
   }
}
