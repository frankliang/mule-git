/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.service;

import org.mule.MuleSessionHandler;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.config.MuleProperties;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.EndpointURIBuilder;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.registry.AbstractServiceDescriptor;
import org.mule.api.service.Service;
import org.mule.api.transaction.TransactionFactory;
import org.mule.api.transformer.Transformer;
import org.mule.api.transport.Connector;
import org.mule.api.transport.MessageAdapter;
import org.mule.api.transport.MessageDispatcherFactory;
import org.mule.api.transport.MessageReceiver;
import org.mule.api.transport.MessageRequesterFactory;
import org.mule.api.transport.SessionHandler;
import org.mule.config.i18n.CoreMessages;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.endpoint.UrlEndpointURIBuilder;
import org.mule.transaction.XaTransactionFactory;
import org.mule.transport.NullPayload;
import org.mule.util.ClassUtils;
import org.mule.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicReference;

/** @inheritDocs */
public class DefaultTransportServiceDescriptor extends AbstractServiceDescriptor implements TransportServiceDescriptor
{
    private String connector;
    private String dispatcherFactory;
    private String requesterFactory;
    private String transactionFactory;
    private String messageAdapter;
    private String messageReceiver;
    private String transactedMessageReceiver;
    private String xaTransactedMessageReceiver;
    private String endpointUriBuilder;
    private String sessionHandler;
    private String defaultInboundTransformer;
    private String defaultOutboundTransformer;
    private String defaultResponseTransformer;
    private String endpointBuilder;

    private final AtomicReference inboundTransformer = new AtomicReference();
    private final AtomicReference outboundTransformer = new AtomicReference();
    private final AtomicReference responseTransformer = new AtomicReference();

    private Properties exceptionMappings = new Properties();
    private MuleContext muleContext;

    private ClassLoader classLoader;

    public DefaultTransportServiceDescriptor(String service, Properties props, ClassLoader classLoader)
    {
        super(service);
        this.classLoader = classLoader;
        init(props);
    }

    protected void init(Properties props)
    {
        connector = removeProperty(MuleProperties.CONNECTOR_CLASS, props);
        dispatcherFactory = removeProperty(MuleProperties.CONNECTOR_DISPATCHER_FACTORY, props);
        requesterFactory = removeProperty(MuleProperties.CONNECTOR_REQUESTER_FACTORY, props);
        transactionFactory = removeProperty(MuleProperties.CONNECTOR_DISPATCHER_FACTORY, props);
        messageReceiver = removeProperty(MuleProperties.CONNECTOR_MESSAGE_RECEIVER_CLASS, props);
        transactedMessageReceiver = removeProperty(MuleProperties.CONNECTOR_TRANSACTED_MESSAGE_RECEIVER_CLASS, props);
        xaTransactedMessageReceiver = removeProperty(MuleProperties.CONNECTOR_XA_TRANSACTED_MESSAGE_RECEIVER_CLASS, props);
        messageAdapter = removeProperty(MuleProperties.CONNECTOR_MESSAGE_ADAPTER, props);
        defaultInboundTransformer = removeProperty(MuleProperties.CONNECTOR_INBOUND_TRANSFORMER, props);
        defaultOutboundTransformer = removeProperty(MuleProperties.CONNECTOR_OUTBOUND_TRANSFORMER, props);
        defaultResponseTransformer = removeProperty(MuleProperties.CONNECTOR_RESPONSE_TRANSFORMER, props);
        endpointBuilder = removeProperty(MuleProperties.CONNECTOR_META_ENDPOINT_BUILDER, props);
        endpointUriBuilder = removeProperty(MuleProperties.CONNECTOR_ENDPOINT_BUILDER, props);
        sessionHandler = removeProperty(MuleProperties.CONNECTOR_SESSION_HANDLER, props);
    }


    public void setOverrides(Properties props)
    {
        if (props == null || props.size() == 0)
        {
            return;
        }

        connector = props.getProperty(MuleProperties.CONNECTOR_CLASS, connector);
        dispatcherFactory = props.getProperty(MuleProperties.CONNECTOR_DISPATCHER_FACTORY, dispatcherFactory);
        requesterFactory = props.getProperty(MuleProperties.CONNECTOR_REQUESTER_FACTORY, requesterFactory);
        messageReceiver = props.getProperty(MuleProperties.CONNECTOR_MESSAGE_RECEIVER_CLASS, messageReceiver);
        transactedMessageReceiver = props.getProperty(
                MuleProperties.CONNECTOR_TRANSACTED_MESSAGE_RECEIVER_CLASS, transactedMessageReceiver);
        xaTransactedMessageReceiver = props.getProperty(
                MuleProperties.CONNECTOR_XA_TRANSACTED_MESSAGE_RECEIVER_CLASS, xaTransactedMessageReceiver);
        messageAdapter = props.getProperty(MuleProperties.CONNECTOR_MESSAGE_ADAPTER, messageAdapter);
        endpointBuilder = props.getProperty(MuleProperties.CONNECTOR_META_ENDPOINT_BUILDER, endpointBuilder);


        String temp = props.getProperty(MuleProperties.CONNECTOR_INBOUND_TRANSFORMER);
        if (temp != null)
        {
            defaultInboundTransformer = temp;
            inboundTransformer.set(null);
        }

        temp = props.getProperty(MuleProperties.CONNECTOR_OUTBOUND_TRANSFORMER);
        if (temp != null)
        {
            defaultOutboundTransformer = temp;
            outboundTransformer.set(null);
        }

        temp = props.getProperty(MuleProperties.CONNECTOR_RESPONSE_TRANSFORMER);
        if (temp != null)
        {
            defaultResponseTransformer = temp;
            responseTransformer.set(null);
        }

        temp = props.getProperty(MuleProperties.CONNECTOR_ENDPOINT_BUILDER);
        if (temp != null)
        {
            endpointUriBuilder = temp;
        }
    }

    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
    }

    public MessageAdapter createMessageAdapter(Object message) throws TransportServiceException
    {
        return createMessageAdapter(message, null, messageAdapter);
    }

    public MessageAdapter createMessageAdapter(Object message, MessageAdapter originalMessageAdapter)
            throws TransportServiceException
    {
        return createMessageAdapter(message, originalMessageAdapter, messageAdapter);
    }

    protected MessageAdapter createMessageAdapter(Object message, MessageAdapter originalMessageAdapter,
                                                  String clazz) throws TransportServiceException
    {
        if (message == null)
        {
            message = NullPayload.getInstance();
        }
        if (messageAdapter != null)
        {
            try
            {
                if (originalMessageAdapter != null)
                {
                    return (MessageAdapter) ClassUtils.instanciateClass(clazz,
                            new Object[]{message, originalMessageAdapter}, classLoader);
                }
                else
                {
                    return (MessageAdapter)
                            ClassUtils.instanciateClass(clazz, new Object[]{message}, classLoader);
                }
            }
            catch (Exception e)
            {
                throw new TransportServiceException(CoreMessages.failedToCreateObjectWith("Message Adapter", clazz), e);
            }
        }
        else
        {
            throw new TransportServiceException(CoreMessages.objectNotSetInService("Message Adapter", getService()));
        }
    }

    public SessionHandler createSessionHandler() throws TransportServiceException
    {
        if (sessionHandler == null)
        {
            sessionHandler = MuleSessionHandler.class.getName();
            if (logger.isDebugEnabled())
            {
                logger.debug("No session.handler set in service description, defaulting to: "
                        + sessionHandler);
            }
        }
        try
        {
            return (SessionHandler) ClassUtils.instanciateClass(sessionHandler, ClassUtils.NO_ARGS, classLoader);
        }
        catch (Throwable e)
        {
            throw new TransportServiceException(CoreMessages.failedToCreateObjectWith("SessionHandler", sessionHandler), e);
        }
    }

    public MessageReceiver createMessageReceiver(Connector connector,
                                                 Service service,
                                                 InboundEndpoint endpoint) throws MuleException
    {

        MessageReceiver mr = createMessageReceiver(connector, service, endpoint, null);
        return mr;
    }

    public MessageReceiver createMessageReceiver(Connector connector,
                                                 Service service,
                                                 InboundEndpoint endpoint,
                                                 Object[] args) throws MuleException
    {
        String receiverClass = messageReceiver;

        if (endpoint.getTransactionConfig().isTransacted())
        {
            boolean xaTx = endpoint.getTransactionConfig().getFactory() instanceof XaTransactionFactory;
            if (transactedMessageReceiver != null && !xaTx)
            {
                receiverClass = transactedMessageReceiver;
            }
            else if (xaTransactedMessageReceiver != null && xaTx)
            {
                receiverClass = xaTransactedMessageReceiver;
            }

        }

        if (receiverClass != null)
        {
            Object[] newArgs;

            if (args != null && args.length != 0)
            {
                newArgs = new Object[3 + args.length];
            }
            else
            {
                newArgs = new Object[3];
            }

            newArgs[0] = connector;
            newArgs[1] = service;
            newArgs[2] = endpoint;

            if (args != null && args.length != 0)
            {
                System.arraycopy(args, 0, newArgs, 3, newArgs.length - 3);
            }

            try
            {
                MessageReceiver mr = (MessageReceiver) ClassUtils.instanciateClass(receiverClass, newArgs, classLoader);
                return mr;
            }
            catch (Exception e)
            {
                throw new TransportServiceException(CoreMessages.failedToCreateObjectWith("Message Receiver", getService()), e);
            }

        }
        else
        {
            throw new TransportServiceException(CoreMessages.objectNotSetInService("Message Receiver", getService()));
        }
    }

    public MessageDispatcherFactory createDispatcherFactory() throws TransportServiceException
    {
        if (dispatcherFactory != null)
        {
            try
            {
                return (MessageDispatcherFactory) ClassUtils.instanciateClass(dispatcherFactory,
                        ClassUtils.NO_ARGS, classLoader);
            }
            catch (Exception e)
            {
                throw new TransportServiceException(CoreMessages.failedToCreateObjectWith("Message Dispatcher Factory", dispatcherFactory), e);
            }
        }
        else
        {
            //Its valid not to have a Dispatcher factory on the transport
            return null;
        }
    }

    public MessageRequesterFactory createRequesterFactory() throws TransportServiceException
    {
        if (requesterFactory != null)
        {
            try
            {
                return (MessageRequesterFactory) ClassUtils.instanciateClass(requesterFactory,
                        ClassUtils.NO_ARGS, classLoader);
            }
            catch (Exception e)
            {
                throw new TransportServiceException(
                        CoreMessages.failedToCreateObjectWith("Message Requester Factory", requesterFactory), e);
            }
        }
        else
        {
            //Its valid not to have a Requester factory on the transport
            return null;
        }
    }

    public TransactionFactory createTransactionFactory() throws TransportServiceException
    {
        if (transactionFactory != null)
        {
            try
            {
                return (TransactionFactory) ClassUtils.instanciateClass(transactionFactory,
                        ClassUtils.NO_ARGS, classLoader);
            }
            catch (Exception e)
            {
                throw new TransportServiceException(CoreMessages.failedToCreateObjectWith("Transaction Factory", transactionFactory), e);
            }
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Connector createConnector() throws TransportServiceException
    {
        Connector newConnector;
        // if there is a factory, use it
        try
        {
            if (connector != null)
            {
                Class<Connector> connectorClass;
                if (classLoader != null)
                {
                    connectorClass = ClassUtils.loadClass(connector, classLoader);
                }
                else
                {
                    connectorClass = ClassUtils.loadClass(connector, getClass());
                }
                newConnector = connectorClass.getConstructor(MuleContext.class).newInstance(muleContext);
            }
            else
            {
                throw new TransportServiceException(CoreMessages.objectNotSetInService("Connector", getService()));
            }
        }
        catch (TransportServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TransportServiceException(CoreMessages.failedToCreateObjectWith("Connector", connector), e);
        }

        if (newConnector.getName() == null)
        {
            newConnector.setName("_" + newConnector.getProtocol() + "Connector#" + connector.hashCode());
        }
        return newConnector;
    }

    @SuppressWarnings("unchecked")
    public List<Transformer> createInboundTransformers() throws TransportFactoryException
    {
        if (inboundTransformer.get() != null)
        {
            return CollectionUtils.singletonList(inboundTransformer.get());
        }
        if (defaultInboundTransformer != null)
        {
            logger.info("Loading default inbound transformer: " + defaultInboundTransformer);
            try
            {
                synchronized (inboundTransformer)
                {
                    if (inboundTransformer.get() == null)
                    {
                        Transformer newTransformer = (Transformer) ClassUtils.instanciateClass(
                            defaultInboundTransformer, ClassUtils.NO_ARGS, classLoader);
                        newTransformer.setName(newTransformer.getName() + "#" + hashCode());
                        muleContext.getRegistry().registerObject(newTransformer.getName(), newTransformer);
                        inboundTransformer.compareAndSet(null, newTransformer);
                    }
                }
                return CollectionUtils.singletonList(inboundTransformer.get());
            }
            catch (Exception e)
            {
                throw new TransportFactoryException(CoreMessages.failedToLoadTransformer("inbound",
                    defaultInboundTransformer), e);
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<Transformer> createOutboundTransformers() throws TransportFactoryException
    {
        if (outboundTransformer.get() != null)
        {
            return CollectionUtils.singletonList(outboundTransformer.get());
        }
        if (defaultOutboundTransformer != null)
        {
            logger.info("Loading default outbound transformer: " + defaultOutboundTransformer);
            try
            {
                synchronized (outboundTransformer)
                {
                    if (outboundTransformer.get() == null)
                    {
                        Transformer newTransformer = (Transformer) ClassUtils.instanciateClass(
                            defaultOutboundTransformer, ClassUtils.NO_ARGS, classLoader);
                        newTransformer.setName(newTransformer.getName() + "#" + hashCode());
                        muleContext.getRegistry().registerObject(newTransformer.getName(), newTransformer);
                        outboundTransformer.compareAndSet(null, newTransformer);
                    }
                }
                return CollectionUtils.singletonList(outboundTransformer.get());
            }
            catch (Exception e)
            {
                throw new TransportFactoryException(CoreMessages.failedToLoadTransformer("outbound",
                    defaultOutboundTransformer), e);
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<Transformer> createResponseTransformers() throws TransportFactoryException
    {
        if (responseTransformer.get() != null)
        {
            return CollectionUtils.singletonList(responseTransformer.get());
        }
        if (defaultResponseTransformer != null)
        {
            logger.info("Loading default response transformer: " + defaultResponseTransformer);
            try
            {
                synchronized (responseTransformer)
                {
                    if (responseTransformer.get() == null)
                    {
                        Transformer newTransformer = (Transformer) ClassUtils.instanciateClass(
                            defaultResponseTransformer, ClassUtils.NO_ARGS, classLoader);
                        newTransformer.setName(newTransformer.getName() + "#" + hashCode());
                        muleContext.getRegistry().registerObject(newTransformer.getName(), newTransformer);
                        responseTransformer.compareAndSet(null, newTransformer);
                    }
                }
                return CollectionUtils.singletonList(responseTransformer.get());
            }
            catch (Exception e)
            {
                throw new TransportFactoryException(CoreMessages.failedToLoadTransformer("response",
                    defaultResponseTransformer), e);
            }
        }
        return Collections.emptyList();
    }

    public EndpointURIBuilder createEndpointURIBuilder() throws TransportFactoryException
    {
        if (endpointUriBuilder == null)
        {
            logger.debug("Endpoint resolver not set, Loading default resolver: "
                    + UrlEndpointURIBuilder.class.getName());
            return new UrlEndpointURIBuilder();
        }
        else
        {
            logger.debug("Loading endpointUri resolver: " + endpointUriBuilder);
            try
            {
                return (EndpointURIBuilder) ClassUtils.instanciateClass(endpointUriBuilder, ClassUtils.NO_ARGS, classLoader);
            }
            catch (Exception e)
            {
                throw new TransportFactoryException(CoreMessages.failedToLoad("EndpointURI Builder: " + endpointUriBuilder), e);
            }
        }
    }

    public EndpointBuilder createEndpointBuilder(String uri) throws TransportFactoryException
    {
        if (endpointBuilder == null)
        {
            logger.debug("Endpoint builder not set, Loading default builder: "
                    + EndpointURIEndpointBuilder.class.getName());
            return new EndpointURIEndpointBuilder(uri, muleContext);
        }
        else
        {
            logger.debug("Loading endpoint builder: " + endpointBuilder);
            try
            {
                return (EndpointBuilder) ClassUtils.instanciateClass(endpointBuilder, new Object[]{uri, muleContext}, classLoader);
            }
            catch (Exception e)
            {
                throw new TransportFactoryException(CoreMessages.failedToLoad("Endpoint Builder: " + endpointBuilder), e);
            }
        }
    }

    public void setExceptionMappings(Properties props)
    {
        this.exceptionMappings = props;
    }

    public Properties getExceptionMappings()
    {
        return this.exceptionMappings;
    }
}
