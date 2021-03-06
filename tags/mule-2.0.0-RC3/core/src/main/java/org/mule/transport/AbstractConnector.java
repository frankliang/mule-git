/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport;

import org.mule.DefaultExceptionStrategy;
import org.mule.MuleSessionHandler;
import org.mule.RegistryContext;
import org.mule.api.MessagingException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleRuntimeException;
import org.mule.api.config.ThreadingProfile;
import org.mule.api.context.WorkManager;
import org.mule.api.context.notification.ServerNotification;
import org.mule.api.context.notification.ServerNotificationHandler;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.DisposeException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.LifecycleTransitionResult;
import org.mule.api.registry.ServiceDescriptorFactory;
import org.mule.api.registry.ServiceException;
import org.mule.api.service.Service;
import org.mule.api.transport.Connectable;
import org.mule.api.transport.ConnectionStrategy;
import org.mule.api.transport.Connector;
import org.mule.api.transport.ConnectorException;
import org.mule.api.transport.DispatchException;
import org.mule.api.transport.MessageAdapter;
import org.mule.api.transport.MessageDispatcher;
import org.mule.api.transport.MessageDispatcherFactory;
import org.mule.api.transport.MessageReceiver;
import org.mule.api.transport.MessageRequester;
import org.mule.api.transport.MessageRequesterFactory;
import org.mule.api.transport.ReplyToHandler;
import org.mule.api.transport.SessionHandler;
import org.mule.config.i18n.CoreMessages;
import org.mule.context.notification.ConnectionNotification;
import org.mule.context.notification.MessageNotification;
import org.mule.context.notification.OptimisedNotificationHandler;
import org.mule.lifecycle.AlreadyInitialisedException;
import org.mule.model.streaming.DelegatingInputStream;
import org.mule.routing.filters.WildcardFilter;
import org.mule.transformer.TransformerUtils;
import org.mule.transport.service.TransportFactory;
import org.mule.transport.service.TransportServiceDescriptor;
import org.mule.transport.service.TransportServiceException;
import org.mule.util.BeanUtils;
import org.mule.util.ClassUtils;
import org.mule.util.CollectionUtils;
import org.mule.util.ObjectNameHelper;
import org.mule.util.ObjectUtils;
import org.mule.util.StringUtils;
import org.mule.util.concurrent.NamedThreadFactory;
import org.mule.util.concurrent.WaitableBoolean;

import java.beans.ExceptionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentMap;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

/**
 * <code>AbstractConnector</code> provides base functionality for all connectors
 * provided with Mule. Connectors are the mechanism used to connect to external
 * systems and protocols in order to send and receive data. <p/> The
 * <code>AbstractConnector</code> provides getter and setter methods for endpoint
 * name, transport name and protocol. It also provides methods to stop and start
 * connecotors and sets up a dispatcher threadpool which allows deriving connectors
 * the possibility to dispatch work to separate threads. This functionality is
 * controlled with the <i> doThreading</i> property on the threadingProfiles for
 * dispachers and receivers. The lifecycle for a connector is -
 * <ol>
 * <li>Create
 * <li>Initialise
 * <li>Connect
 * <li>Connect receivers
 * <li>Start
 * <li>Start Receivers
 * <li>Stop
 * <li>Stop Receivers
 * <li>Disconnect
 * <li>Disconnect Receivers
 * <li>Dispose
 * <li>Dispose Receivers
 * </ol>
 */
public abstract class AbstractConnector
    implements Connector, ExceptionListener, Connectable, WorkListener
{
    /**
     * Default number of concurrent transactional receivers.
     */
    public static final int DEFAULT_NUM_CONCURRENT_TX_RECEIVERS = 4;

    /**
     * logger used by this class
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Specifies if the endpoint started
     */
    protected final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * True once the endpoint has been initialsed
     */
    protected final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * The name that identifies the endpoint
     */
    protected volatile String name;

    /**
     * The exception strategy used by this connector
     */
    protected volatile ExceptionListener exceptionListener;

    /**
     * Determines in the connector is alive and well
     */
    protected final AtomicBoolean disposed = new AtomicBoolean(false);

    /**
     * Determines in connector has been told to dispose
     */
    protected final AtomicBoolean disposing = new AtomicBoolean(false);

    /**
     * Factory used to create dispatchers for this connector
     */
    protected volatile MessageDispatcherFactory dispatcherFactory;

    /**
     * Factory used to create requesters for this connector
     */
    protected volatile MessageRequesterFactory requesterFactory;

    /**
     * A pool of dispatchers for this connector, keyed by endpoint
     */
    protected final GenericKeyedObjectPool dispatchers = new GenericKeyedObjectPool();

    /**
     * A pool of requesters for this connector, keyed by endpoint
     */
    protected final GenericKeyedObjectPool requesters = new GenericKeyedObjectPool();

    /**
     * The collection of listeners on this connector. Keyed by entrypoint
     */
    protected final ConcurrentMap receivers = new ConcurrentHashMap();

    /**
     * Defines the dispatcher threading profile
     */
    private volatile ThreadingProfile dispatcherThreadingProfile;
    
    /**
     * Defines the requester threading profile
     */
    private volatile ThreadingProfile requesterThreadingProfile;
    
    /**
     * Defines the receiver threading profile
     */
    private volatile ThreadingProfile receiverThreadingProfile;
    
    /**
     * @see {@link #isCreateMultipleTransactedReceivers()}
     */
    protected volatile boolean createMultipleTransactedReceivers = true;

    /**
     * @see {@link #getNumberOfConcurrentTransactedReceivers()}
     */
    protected volatile int numberOfConcurrentTransactedReceivers = DEFAULT_NUM_CONCURRENT_TX_RECEIVERS;


    protected volatile ConnectionStrategy connectionStrategy;

    protected final WaitableBoolean connected = new WaitableBoolean(false);

    protected final WaitableBoolean connecting = new WaitableBoolean(false);

    /**
     * If the connect method was called via the start method, this will be set so
     * that when the connector comes on line it will be started
     */
    protected final WaitableBoolean startOnConnect = new WaitableBoolean(false);

    /**
     * Optimise the handling of message notifications.  If dynamic is set to false then the
     * cached notification handler implements a shortcut for message notifications.
     */
    private boolean dynamicNotification = false;
    private ServerNotificationHandler cachedNotificationHandler;

    private final List supportedProtocols;

    /**
     * A shared work manager for all receivers registered with this connector.
     */
    private final AtomicReference/*<WorkManager>*/ receiverWorkManager = new AtomicReference();

    /**
     * A shared work manager for all requesters created for this connector.
     */
    private final AtomicReference/*<WorkManager>*/ dispatcherWorkManager = new AtomicReference();

    /**
     * A shared work manager for all requesters created for this connector.
     */
    private final AtomicReference/*<WorkManager>*/ requesterWorkManager = new AtomicReference();

    /**
     * A generic scheduling service for tasks that need to be performed periodically.
     */
    private final AtomicReference/*<ScheduledExecutorService>*/ scheduler = new AtomicReference();

    /**
     * Holds the service configuration for this connector
     */
    protected volatile TransportServiceDescriptor serviceDescriptor;

    /**
     * The map of service overrides that can be used to extend the capabilities of the
     * connector
     */
    protected volatile Properties serviceOverrides;

    /**
     * The strategy used for reading and writing session information to and fromt he
     * transport
     */
    protected volatile SessionHandler sessionHandler = new MuleSessionHandler();

    protected MuleContext muleContext;

    public AbstractConnector()
    {
        setDynamicNotification(false);

        // always add at least the default protocol
        supportedProtocols = new ArrayList();
        supportedProtocols.add(getProtocol().toLowerCase());

        connectionStrategy = new SingleAttemptConnectionStrategy();

        // TODO dispatcher pool configuration should be extracted, maybe even
        // moved into the factory?
        // NOTE: testOnBorrow MUST be FALSE. this is a bit of a design bug in
        // commons-pool since validate is used for both activation and passivation,
        // but has no way of knowing which way it is going.
        dispatchers.setTestOnBorrow(false);
        dispatchers.setTestOnReturn(true);
        requesters.setTestOnBorrow(false);
        requesters.setTestOnReturn(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.transport.UMOConnector#getName()
     */
    public String getName()
    {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.transport.UMOConnector#setName(java.lang.String)
     */
    public void setName(String newName)
    {
        if (newName == null)
        {
            throw new IllegalArgumentException(CoreMessages.objectIsNull("Connector name").toString());
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Set Connector name to: " + newName);
        }

        name = newName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.transport.UMOConnector#create(java.util.HashMap)
     */
    public final synchronized LifecycleTransitionResult initialise() throws InitialisationException
    {
        if (initialised.get())
        {
            InitialisationException e = new AlreadyInitialisedException("Connector '" + getProtocol() + "." + getName() + "'", this);
            throw e;
            // Just log a warning since initializing twice is bad but might not be the end of the world.
            //logger.warn(e);
        }

        if (logger.isInfoEnabled())
        {
            logger.info("Initialising: " + this);
        }

        // Use lazy-init (in get() methods) for this instead.
        //dispatcherThreadingProfile = muleContext.getDefaultMessageDispatcherThreadingProfile();
        //requesterThreadingProfile = muleContext.getDefaultMessageRequesterThreadingProfile();
        //receiverThreadingProfile = muleContext.getDefaultMessageReceiverThreadingProfile();

        // Initialise the structure of this connector
        this.initFromServiceDescriptor();

        this.doInitialise();

        // We do the management context injection here just in case we're using a default ExceptionStrategy
        //We always create a default just in case anything goes wrong before
        if(exceptionListener==null)
        {
            exceptionListener = new DefaultExceptionStrategy();
            ((DefaultExceptionStrategy)exceptionListener).setMuleContext(muleContext);
            ((DefaultExceptionStrategy)exceptionListener).initialise();
        }

        try
        {
            initWorkManagers();
        }
        catch (MuleException e)
        {
            throw new InitialisationException(e, this);
        }

        initialised.set(true);

        return LifecycleTransitionResult.OK;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.api.transport.Connector#start()
     */
    public final synchronized LifecycleTransitionResult start() throws MuleException
    {
        this.checkDisposed();

        if (!this.isStarted())
        {
            //TODO: Not sure about this.  Right now the connector will connect only once
            // there is an endpoint associated with it and that endpoint is connected.
            // Do we also need the option of connecting the connector without any endpoints?
//            if (!this.isConnected())
//            {
//                startOnConnect.set(true);
//                this.getConnectionStrategy().connect(this);
//                // Only start once we are connected
//                return;
//            }
            if (!this.isConnected())
            {
                startOnConnect.set(true);
                // Don't call getConnectionStrategy(), it clones the connection strategy.
                // Connectors should have a single reconnection thread, unlike per receiver/dispatcher
                connectionStrategy.connect(this);
                // Only start once we are connected
                return LifecycleTransitionResult.OK;
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Starting: " + this);
            }

            // the scheduler is recreated after stop()
            ScheduledExecutorService currentScheduler = (ScheduledExecutorService) scheduler.get();
            if (currentScheduler == null || currentScheduler.isShutdown())
            {
                scheduler.set(this.getScheduler());
            }

            this.doStart();
            started.set(true);

            if (receivers != null)
            {
                for (Iterator iterator = receivers.values().iterator(); iterator.hasNext();)
                {
                    MessageReceiver mr = (MessageReceiver) iterator.next();
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Starting receiver on endpoint: " + mr.getEndpoint().getEndpointURI());
                    }
                    mr.start();
                }
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Started: " + this);
            }
        }
        return LifecycleTransitionResult.OK;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.api.transport.Connector#isStarted()
     */
    public boolean isStarted()
    {
        return started.get();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.api.transport.Connector#stop()
     */
    public final synchronized LifecycleTransitionResult stop() throws MuleException
    {
        if (this.isDisposed())
        {
            return LifecycleTransitionResult.OK;
        }

        if (this.isStarted())
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Stopping: " + this);
            }

            // shutdown our scheduler service
            ((ScheduledExecutorService) scheduler.get()).shutdown();

            this.doStop();
            started.set(false);

            // Stop all the receivers on this connector (this will cause them to
            // disconnect too)
            if (receivers != null)
            {
                for (Iterator iterator = receivers.values().iterator(); iterator.hasNext();)
                {
                    MessageReceiver mr = (MessageReceiver) iterator.next();
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Stopping receiver on endpoint: " + mr.getEndpoint().getEndpointURI());
                    }
                    mr.stop();
                }
            }
        }

        if (this.isConnected())
        {
            try
            {
                this.disconnect();
            }
            catch (Exception e)
            {
                // TODO MULE-863: What should we really do?
                logger.error("Failed to disconnect: " + e.getMessage(), e);
            }
        }

        // make sure the scheduler is gone
        scheduler.set(null);

        // we do not need to stop the work managers because they do no harm (will just be idle)
        // and will be reused on restart without problems.

        //TODO RM* THis shouldn't be here this.initialised.set(false);
        // started=false already issued above right after doStop()
        if (logger.isInfoEnabled())
        {
            logger.info("Stopped: " + this);
        }
        return LifecycleTransitionResult.OK;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.api.transport.Connector#shutdown()
     */
    public final synchronized void dispose()
    {
        disposing.set(true);

        if (logger.isInfoEnabled())
        {
            logger.info("Disposing: " + this);
        }

        try
        {
            this.stop();
        }
        catch (MuleException e)
        {
            // TODO MULE-863: What should we really do?
            logger.warn("Failed to stop during shutdown: " + e.getMessage(), e);
        }

        this.disposeReceivers();
        this.disposeDispatchers();
        this.disposeRequesters();
        this.disposeWorkManagers();

        this.doDispose();
        disposed.set(true);
        initialised.set(false);

        if (logger.isInfoEnabled())
        {
            logger.info("Disposed: " + this);
        }
    }

    protected void initWorkManagers() throws MuleException
    {
        if (receiverWorkManager.get() == null)
        {
            WorkManager newWorkManager = this.getReceiverThreadingProfile().createWorkManager(
                getName() + ".receiver");

            if (receiverWorkManager.compareAndSet(null, newWorkManager))
            {
                newWorkManager.start();
            }
        }

        if (dispatcherWorkManager.get() == null)
        {
            WorkManager newWorkManager = this.getDispatcherThreadingProfile().createWorkManager(
                getName() + ".dispatcher");

            if (dispatcherWorkManager.compareAndSet(null, newWorkManager))
            {
                newWorkManager.start();
            }
        }
    }
    protected void disposeWorkManagers()
    {
        logger.debug("Disposing dispatcher work manager");
        WorkManager workManager = (WorkManager) dispatcherWorkManager.get();
        if (workManager != null)
        {
            workManager.dispose();
        }
        dispatcherWorkManager.set(null);

        logger.debug("Disposing receiver work manager");
        workManager = (WorkManager) receiverWorkManager.get();
        if (workManager != null)
        {
            workManager.dispose();
        }
        receiverWorkManager.set(null);
    }

    protected void disposeReceivers()
    {
        if (receivers != null)
        {
            logger.debug("Disposing Receivers");

            for (Iterator iterator = receivers.values().iterator(); iterator.hasNext();)
            {
                MessageReceiver receiver = (MessageReceiver) iterator.next();

                try
                {
                    this.destroyReceiver(receiver, receiver.getEndpoint());
                }
                catch (Throwable e)
                {
                    // TODO MULE-863: What should we really do?
                    logger.error("Failed to destroy receiver: " + receiver, e);
                }
            }

            receivers.clear();
            logger.debug("Receivers Disposed");
        }
    }

    protected void disposeDispatchers()
    {
        if (dispatchers != null)
        {
            logger.debug("Disposing Dispatchers");
            dispatchers.clear();
            logger.debug("Dispatchers Disposed");
        }
    }

    protected void disposeRequesters()
    {
        if (requesters != null)
        {
            logger.debug("Disposing Requesters");
             requesters.clear();
            logger.debug("Requesters Disposed");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.api.transport.Connector#isAlive()
     */
    public boolean isDisposed()
    {
        return disposed.get();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.api.transport.Connector#handleException(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void handleException(Exception exception)
    {
        if (exceptionListener == null)
        {
            throw new MuleRuntimeException(
                CoreMessages.exceptionOnConnectorNotExceptionListener(this.getName()), exception);
        }
        else
        {
            exceptionListener.exceptionThrown(exception);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.util.ExceptionListener#onException(java.lang.Throwable)
     */
    public void exceptionThrown(Exception e)
    {
        handleException(e);
    }

    /**
     * @return the ExceptionStrategy for this endpoint
     * @see ExceptionListener
     */
    public ExceptionListener getExceptionListener()
    {
        return exceptionListener;
    }

    /**
     * @param listener the ExceptionStrategy to use with this endpoint
     * @see ExceptionListener
     */
    public void setExceptionListener(ExceptionListener listener)
    {
        exceptionListener = listener;
    }

    /**
     * @return Returns the dispatcherFactory.
     */
    public MessageDispatcherFactory getDispatcherFactory()
    {
        return dispatcherFactory;
    }

    /**
     * @param dispatcherFactory The dispatcherFactory to set.
     */
    public void setDispatcherFactory(MessageDispatcherFactory dispatcherFactory)
    {
        KeyedPoolableObjectFactory poolFactory;

        if (dispatcherFactory instanceof KeyedPoolableObjectFactory)
        {
            poolFactory = (KeyedPoolableObjectFactory) dispatcherFactory;
        }
        else
        {
            // need to adapt the factory for use by commons-pool
            poolFactory = new KeyedPoolMessageDispatcherFactoryAdapter(dispatcherFactory);
        }

        this.dispatchers.setFactory(poolFactory);

        // we keep a reference to the unadapted factory, otherwise people might end
        // up with ClassCastExceptions on downcast to their implementation (sigh)
        this.dispatcherFactory = dispatcherFactory;
    }

    /**
     * @return Returns the requesterFactory.
     */
    public MessageRequesterFactory getRequesterFactory()
    {
        return requesterFactory;
    }

    /**
     * @param requesterFactory The requesterFactory to set.
     */
    public void setRequesterFactory(MessageRequesterFactory requesterFactory)
    {
        KeyedPoolableObjectFactory poolFactory;

        if (requesterFactory instanceof KeyedPoolableObjectFactory)
        {
            poolFactory = (KeyedPoolableObjectFactory) requesterFactory;
        }
        else
        {
            // need to adapt the factory for use by commons-pool
            poolFactory = new KeyedPoolMessageRequesterFactoryAdapter(requesterFactory);
        }

        requesters.setFactory(poolFactory);

        // we keep a reference to the unadapted factory, otherwise people might end
        // up with ClassCastExceptions on downcast to their implementation (sigh)
        this.requesterFactory = requesterFactory;
    }

    /**
     * Returns the maximum number of dispatchers that can be concurrently active per
     * endpoint.
     *
     * @return max. number of active dispatchers
     */
    public int getMaxDispatchersActive()
    {
        return this.dispatchers.getMaxActive();
    }

    /**
     * Configures the maximum number of dispatchers that can be concurrently active
     * per endpoint
     *
     * @param maxActive max. number of active dispatchers
     */
    public void setMaxDispatchersActive(int maxActive)
    {
        this.dispatchers.setMaxActive(maxActive);
        // adjust maxIdle in tandem to avoid thrashing
        this.dispatchers.setMaxIdle(maxActive);
    }

    private MessageDispatcher getDispatcher(OutboundEndpoint endpoint) throws MuleException
    {
        this.checkDisposed();

        if (endpoint == null)
        {
            throw new IllegalArgumentException("Endpoint must not be null");
        }

        if (!supportsProtocol(endpoint.getConnector().getProtocol()))
        {
            throw new IllegalArgumentException(
                CoreMessages.connectorSchemeIncompatibleWithEndpointScheme(this.getProtocol(),
                    endpoint.getEndpointURI().toString()).getMessage());
        }

        MessageDispatcher dispatcher = null;
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Borrowing a dispatcher for endpoint: " + endpoint.getEndpointURI());
            }

            dispatcher = (MessageDispatcher)dispatchers.borrowObject(endpoint);

            if (logger.isDebugEnabled())
            {
                logger.debug("Borrowed a dispatcher for endpoint: " + endpoint.getEndpointURI() + " = "
                                + dispatcher.toString());
            }

            return dispatcher;
        }
        catch (Exception ex)
        {
            throw new ConnectorException(CoreMessages.connectorCausedError(), this, ex);
        }
        finally
        {
            try
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Borrowed dispatcher: " + ObjectUtils.toString(dispatcher, "null"));
                }
            }
            catch (Exception ex)
            {
                throw new ConnectorException(CoreMessages.connectorCausedError(), this, ex);
            }
        }
    }

    private void returnDispatcher(OutboundEndpoint endpoint, MessageDispatcher dispatcher)
    {
        if (endpoint != null && dispatcher != null)
        {
            try
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Returning dispatcher for endpoint: " + endpoint.getEndpointURI() + " = "
                                    + dispatcher.toString());
                }

            }
            catch (Exception ex)
            {
                //Logging failed
            }
            finally
            {
                try
                {
                    dispatchers.returnObject(endpoint, dispatcher);
                }
                catch (Exception e)
                {
                    // TODO MULE-863: What should we really do?
                    // ignore - if the dispatcher is broken, it will likely get cleaned
                    // up by the factory
                    //RM* I think we should at least log this error so give some indication of what is failing
                    logger.error("Failed to dispose dispatcher for endpoint: " + endpoint +
                            ". This will cause a memory leak. Please report to", e);
                }
            }
        }
    }

    /**
     * Returns the maximum number of requesters that can be concurrently active per
     * endpoint.
     *
     * @return max. number of active requesters
     */
    public int getMaxRequestersActive()
    {
        return this.requesters.getMaxActive();
    }

    /**
     * Configures the maximum number of requesters that can be concurrently active
     * per endpoint
     *
     * @param maxActive max. number of active requesters
     */
    public void setMaxRequestersActive(int maxActive)
    {
        this.requesters.setMaxActive(maxActive);
        // adjust maxIdle in tandem to avoid thrashing
        this.requesters.setMaxIdle(maxActive);
    }

    private MessageRequester getRequester(InboundEndpoint endpoint) throws MuleException
    {
        this.checkDisposed();

        if (endpoint == null)
        {
            throw new IllegalArgumentException("Endpoint must not be null");
        }

        if (!supportsProtocol(endpoint.getConnector().getProtocol()))
        {
            throw new IllegalArgumentException(
                CoreMessages.connectorSchemeIncompatibleWithEndpointScheme(this.getProtocol(),
                    endpoint.getEndpointURI().toString()).getMessage());
        }

        MessageRequester requester = null;
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Borrowing a requester for endpoint: " + endpoint.getEndpointURI());
            }

            requester = (MessageRequester)requesters.borrowObject(endpoint);

            if (logger.isDebugEnabled())
            {
                logger.debug("Borrowed a requester for endpoint: " + endpoint.getEndpointURI() + " = "
                                + requester.toString());
            }

            return requester;
        }
        catch (Exception ex)
        {
            throw new ConnectorException(CoreMessages.connectorCausedError(), this, ex);
        }
        finally
        {
            try
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Borrowed requester: " + ObjectUtils.toString(requester, "null"));
                }
            }
            catch (Exception ex)
            {
                throw new ConnectorException(CoreMessages.connectorCausedError(), this, ex);
            }
        }
    }

    private void returnRequester(InboundEndpoint endpoint, MessageRequester requester)
    {
        if (endpoint != null && requester != null)
        {
            try
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Returning requester for endpoint: " + endpoint.getEndpointURI() + " = "
                                    + requester.toString());
                }

            }
            catch (Exception ex)
            {
                //Logging failed
            }
            finally
            {
                try
                {
                    requesters.returnObject(endpoint, requester);
                }
                catch (Exception e)
                {
                    // TODO MULE-863: What should we really do?
                    // ignore - if the requester is broken, it will likely get cleaned
                    // up by the factory
                    //RM* I think we should at least log this error so give some indication of what is failing
                    logger.error("Failed to dispose requester for endpoint: " + endpoint +
                            ". This will cause a memory leak. Please report to", e);
                }
            }
        }
    }

    protected void checkDisposed() throws DisposeException
    {
        if (this.isDisposed())
        {
            throw new DisposeException(CoreMessages.cannotUseDisposedConnector(), this);
        }
    }

    public MessageReceiver registerListener(Service service, InboundEndpoint endpoint) throws Exception
    {
        if (endpoint == null)
        {
            throw new IllegalArgumentException("The endpoint cannot be null when registering a listener");
        }

        if (service == null)
        {
            throw new IllegalArgumentException("The service cannot be null when registering a listener");
        }

        EndpointURI endpointUri = endpoint.getEndpointURI();
        if (endpointUri == null)
        {
            throw new ConnectorException(CoreMessages.endpointIsNullForListener(), this);
        }

        logger.info("Registering listener: " + service.getName() + " on endpointUri: "
                        + endpointUri.toString());

        if (getReceiver(service, endpoint) != null)
        {
            throw new ConnectorException(CoreMessages.listenerAlreadyRegistered(endpointUri), this);
        }

        MessageReceiver receiver = createReceiver(service, endpoint);
        Object receiverKey = getReceiverKey(service, endpoint);
        receiver.setReceiverKey(receiverKey.toString());
        // Since we're managing the creation we also need to initialise
        receiver.initialise();
        receivers.put(receiverKey, receiver);
        // receivers.put(getReceiverKey(service, endpoint), receiver);

        return receiver;
    }

    /**
     * The method determines the key used to store the receiver against.
     *
     * @param service the service for which the endpoint is being registered
     * @param endpoint the endpoint being registered for the service
     * @return the key to store the newly created receiver against
     */
    protected Object getReceiverKey(Service service, InboundEndpoint endpoint)
    {
        return StringUtils.defaultIfEmpty(endpoint.getEndpointURI().getFilterAddress(), endpoint
            .getEndpointURI().getAddress());
    }

    public final void unregisterListener(Service service, InboundEndpoint endpoint) throws Exception
    {
        if (service == null)
        {
            throw new IllegalArgumentException(
                "The service must not be null when you unregister a listener");
        }

        if (endpoint == null)
        {
            throw new IllegalArgumentException("The endpoint must not be null when you unregister a listener");
        }

        EndpointURI endpointUri = endpoint.getEndpointURI();
        if (endpointUri == null)
        {
            throw new IllegalArgumentException(
                "The endpointUri must not be null when you unregister a listener");
        }

        if (logger.isInfoEnabled())
        {
            logger.info("Removing listener on endpointUri: " + endpointUri);
        }

        if (receivers != null && !receivers.isEmpty())
        {
            MessageReceiver receiver = (MessageReceiver)receivers.remove(getReceiverKey(service,
                endpoint));
            if (receiver != null)
            {
                destroyReceiver(receiver, endpoint);
                receiver.dispose();
            }
        }
    }

    /**
     * Getter for property 'dispatcherThreadingProfile'.
     *
     * @return Value for property 'dispatcherThreadingProfile'.
     */
    public ThreadingProfile getDispatcherThreadingProfile()
    {
        if (dispatcherThreadingProfile == null && muleContext != null)
        {
            dispatcherThreadingProfile = muleContext.getDefaultMessageDispatcherThreadingProfile();
        }
        return dispatcherThreadingProfile;
    }

    /**
     * Setter for property 'dispatcherThreadingProfile'.
     *
     * @param dispatcherThreadingProfile Value to set for property
     *            'dispatcherThreadingProfile'.
     */
    public void setDispatcherThreadingProfile(ThreadingProfile dispatcherThreadingProfile)
    {
        this.dispatcherThreadingProfile = dispatcherThreadingProfile;
    }

    /**
     * Getter for property 'requesterThreadingProfile'.
     *
     * @return Value for property 'requesterThreadingProfile'.
     */
    public ThreadingProfile getRequesterThreadingProfile()
    {
        if (requesterThreadingProfile == null && muleContext != null)
        {
            requesterThreadingProfile = muleContext.getDefaultMessageRequesterThreadingProfile();
        }
        return requesterThreadingProfile;
    }

    /**
     * Setter for property 'requesterThreadingProfile'.
     *
     * @param requesterThreadingProfile Value to set for property
     *            'requesterThreadingProfile'.
     */
    public void setRequesterThreadingProfile(ThreadingProfile requesterThreadingProfile)
    {
        this.requesterThreadingProfile = requesterThreadingProfile;
    }

    /**
     * Getter for property 'receiverThreadingProfile'.
     *
     * @return Value for property 'receiverThreadingProfile'.
     */
    public ThreadingProfile getReceiverThreadingProfile()
    {
        if (receiverThreadingProfile == null && muleContext != null)
        {
            receiverThreadingProfile = muleContext.getDefaultMessageReceiverThreadingProfile();
        }
        return receiverThreadingProfile;
    }

    /**
     * Setter for property 'receiverThreadingProfile'.
     *
     * @param receiverThreadingProfile Value to set for property
     *            'receiverThreadingProfile'.
     */
    public void setReceiverThreadingProfile(ThreadingProfile receiverThreadingProfile)
    {
        this.receiverThreadingProfile = receiverThreadingProfile;
    }

    public void destroyReceiver(MessageReceiver receiver, InboundEndpoint endpoint) throws Exception
    {
        receiver.dispose();
    }

    protected abstract void doInitialise() throws InitialisationException;

    /**
     * Template method to perform any work when destroying the connectoe
     */
    protected abstract void doDispose();

    /**
     * Template method to perform any work when starting the connectoe
     *
     * @throws MuleException if the method fails
     */
    protected abstract void doStart() throws MuleException;

    /**
     * Template method to perform any work when stopping the connectoe
     *
     * @throws MuleException if the method fails
     */
    protected abstract void doStop() throws MuleException;

    public List getDefaultInboundTransformers()
    {
        if (serviceDescriptor == null)
        {
            throw new RuntimeException("serviceDescriptor not initialized");
        }
        return TransformerUtils.getDefaultInboundTransformers(serviceDescriptor);
    }

    public List getDefaultResponseTransformers()
    {
        if (serviceDescriptor == null)
        {
            throw new RuntimeException("serviceDescriptor not initialized");
        }
        return TransformerUtils.getDefaultResponseTransformers(serviceDescriptor);
    }

    public List getDefaultOutboundTransformers()
    {
        if (serviceDescriptor == null)
        {
            throw new RuntimeException("serviceDescriptor not initialized");
        }
        return TransformerUtils.getDefaultOutboundTransformers(serviceDescriptor);
    }

    /**
     * Getter for property 'replyToHandler'.
     *
     * @return Value for property 'replyToHandler'.
     */
    public ReplyToHandler getReplyToHandler()
    {
        return new DefaultReplyToHandler(getDefaultResponseTransformers());
    }

    /**
     * Fires a server notification to all registered listeners
     *
     * @param notification the notification to fire.
     */
    public void fireNotification(ServerNotification notification)
    {
        cachedNotificationHandler.fireNotification(notification);
    }

    /**
     * Getter for property 'connectionStrategy'.
     *
     * @return Value for property 'connectionStrategy'.
     */
    //TODO RM* REMOVE
    public ConnectionStrategy getConnectionStrategy()
    {
        // not happy with this but each receiver needs its own instance
        // of the connection strategy and using a factory just introduces extra
        // implementation
        try
        {
            return (ConnectionStrategy) BeanUtils.cloneBean(connectionStrategy);
        }
        catch (Exception e)
        {
            throw new MuleRuntimeException(CoreMessages.failedToClone("connectionStrategy"), e);
        }
    }

    /**
     * Setter for property 'connectionStrategy'.
     *
     * @param connectionStrategy Value to set for property 'connectionStrategy'.
     */
    public void setConnectionStrategy(ConnectionStrategy connectionStrategy)
    {
        this.connectionStrategy = connectionStrategy;
    }

    /** {@inheritDoc} */
    public boolean isDisposing()
    {
        return disposing.get();
    }

    public boolean isRemoteSyncEnabled()
    {
        return false;
    }
    
    public boolean isSyncEnabled(String protocol)
    {
        return false;
    }

    public MessageReceiver getReceiver(Service service, InboundEndpoint endpoint)
    {
        if (receivers != null)
        {
            Object key = getReceiverKey(service, endpoint);
            if (key != null)
            {
                return (MessageReceiver) receivers.get(key);
            }
            else
            {
                throw new RuntimeException("getReceiverKey() returned a null key");
            }
        }
        else 
        {
            throw new RuntimeException("Connector has not been initialized.");
        }
    }

    /**
     * Getter for property 'receivers'.
     *
     * @return Value for property 'receivers'.
     */
    public Map getReceivers()
    {
        return Collections.unmodifiableMap(receivers);
    }

    public MessageReceiver lookupReceiver(String key)
    {
        if (key != null)
        {
            return (MessageReceiver) receivers.get(key);
        }
        else
        {
            throw new IllegalArgumentException("Receiver key must not be null");
        }
    }

    public MessageReceiver[] getReceivers(String wildcardExpression)
    {
        WildcardFilter filter = new WildcardFilter(wildcardExpression);
        filter.setCaseSensitive(false);

        List found = new ArrayList();

        for (Iterator iterator = receivers.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry e = (Map.Entry) iterator.next();
            if (filter.accept(e.getKey()))
            {
                found.add(e.getValue());
            }
        }

        return (MessageReceiver[]) CollectionUtils.toArrayOfComponentType(found,
            MessageReceiver.class);
    }

    public void connect() throws Exception
    {
        this.checkDisposed();

        if (connected.get())
        {
            return;
        }

        /*
            Until the recursive startConnector() -> connect() -> doConnect() -> connect()
            calls are unwound between a connector and connection strategy, this call has
            to be here, and not below (commented out currently). Otherwise, e.g. WebspherMQ
            goes into an endless reconnect thrashing loop, see MULE-1150 for more details.
        */
        try
        {
            if (connecting.get())
            {
                this.doConnect();
            }
            if (connecting.compareAndSet(false, true))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Connecting: " + this);
                }

                connectionStrategy.connect(this);

                logger.info("Connected: " + getConnectionDescription());
                // This method calls itself so the connecting flag is set first, then
                // the connection is made on the second call
                return;
            }


            // see the explanation above
            //this.doConnect();
            connected.set(true);
            connecting.set(false);

            this.fireNotification(new ConnectionNotification(this, getConnectEventId(),
                ConnectionNotification.CONNECTION_CONNECTED));
        }
        catch (Exception e)
        {
            connected.set(false);
            connecting.set(false);

            this.fireNotification(new ConnectionNotification(this, getConnectEventId(),
                ConnectionNotification.CONNECTION_FAILED));

            if (e instanceof ConnectException || e instanceof FatalConnectException)
            {
                // rethrow
                throw e;
            }
            else
            {
                throw new ConnectException(e, this);
            }
        }

        if (startOnConnect.get())
        {
            this.start();
        }
        //TODO RM*. If the connection strategy is called on the receivers, the connector strategy gets called too,
        //to ensure its connected. Therefore the connect method on the connector needs to be idempotent and not try
        //and connect dispatchers or receivers

//        else
//        {
//            for (Iterator iterator = receivers.values().iterator(); iterator.hasNext();)
//            {
//                MessageReceiver receiver = (MessageReceiver)iterator.next();
//                if (logger.isDebugEnabled())
//                {
//                    logger.debug("Connecting receiver on endpoint: "
//                                    + receiver.getEndpoint().getEndpointURI());
//                }
//                receiver.connect();
//            }
//        }
    }

    public void disconnect() throws Exception
    {
        startOnConnect.set(this.isStarted());

        this.fireNotification(new ConnectionNotification(this, getConnectEventId(),
            ConnectionNotification.CONNECTION_DISCONNECTED));

        connected.set(false);

        try
        {
            this.doDisconnect();
        }
        finally
        {
            this.stop();
        }

        logger.info("Disconnected: " + this.getConnectionDescription());
    }

    public String getConnectionDescription()
    {
        return this.toString();
    }

    public final boolean isConnected()
    {
        return connected.get();
    }

    /**
     * Template method where any connections should be made for the connector
     *
     * @throws Exception
     */
    protected abstract void doConnect() throws Exception;

    /**
     * Template method where any connected resources used by the connector should be
     * disconnected
     *
     * @throws Exception
     */
    protected abstract void doDisconnect() throws Exception;

    /**
     * The resource id used when firing ConnectEvents from this connector
     *
     * @return the resource id used when firing ConnectEvents from this connector
     */
    protected String getConnectEventId()
    {
        return getName();
    }

    /**
     * For better throughput when using TransactedMessageReceivers this will enable a
     * number of concurrent receivers, based on the value returned by
     * {@link #getNumberOfConcurrentTransactedReceivers()}. This property is used by
     * transports that support transactions, specifically receivers that extend the
     * TransactedPollingMessageReceiver.
     *
     * @return true if multiple receivers will be enabled for this connection
     */
    public boolean isCreateMultipleTransactedReceivers()
    {
        return createMultipleTransactedReceivers;
    }

    /**
     * @see {@link #isCreateMultipleTransactedReceivers()}
     * @param createMultipleTransactedReceivers if true, multiple receivers will be
     *            created for this connection
     */
    public void setCreateMultipleTransactedReceivers(boolean createMultipleTransactedReceivers)
    {
        this.createMultipleTransactedReceivers = createMultipleTransactedReceivers;
    }

    /**
     * Returns the number of concurrent receivers that will be launched when
     * {@link #isCreateMultipleTransactedReceivers()} returns <code>true</code>.
     *
     * @see #DEFAULT_NUM_CONCURRENT_TX_RECEIVERS
     */
    public int getNumberOfConcurrentTransactedReceivers()
    {
        return numberOfConcurrentTransactedReceivers;
    }

    /**
     * @see {@link #getNumberOfConcurrentTransactedReceivers()}
     * @param count the number of concurrent transacted receivers to start
     */
    public void setNumberOfConcurrentTransactedReceivers(int count)
    {
        numberOfConcurrentTransactedReceivers = count;
    }

    public void setDynamicNotification(boolean dynamic)
    {
        dynamicNotification = dynamic;
    }

    protected void updateCachedNotificationHandler()
    {
        if (null != muleContext)
        {
            if (dynamicNotification)
            {
                cachedNotificationHandler = muleContext.getNotificationManager();
            }
            else
            {
                cachedNotificationHandler =
                        new OptimisedNotificationHandler(muleContext.getNotificationManager(), MessageNotification.class);
            }
        }
    }

    public boolean isEnableMessageEvents()
    {
        return cachedNotificationHandler.isNotificationEnabled(MessageNotification.class);
    }

    /**
     * Registers other protocols 'understood' by this connector. These must contain
     * scheme meta info. Any protocol registered must begin with the protocol of this
     * connector, i.e. If the connector is axis the protocol for jms over axis will
     * be axis:jms. Here, 'axis' is the scheme meta info and 'jms' is the protocol.
     * If the protocol argument does not start with the connector's protocol, it will
     * be appended.
     *
     * @param protocol the supported protocol to register
     */
    public void registerSupportedProtocol(String protocol)
    {
        protocol = protocol.toLowerCase();
        if (protocol.startsWith(getProtocol().toLowerCase()))
        {
            registerSupportedProtocolWithoutPrefix(protocol);
        }
        else
        {
            supportedProtocols.add(getProtocol().toLowerCase() + ":" + protocol);
        }
    }

    /**
     * Registers other protocols 'understood' by this connector. These must contain
     * scheme meta info. Unlike the {@link #registerSupportedProtocol(String)} method,
     * this allows you to register protocols that are not prefixed with the connector
     * protocol. This is useful where you use a Service Finder to discover which
     * Transport implementation to use. For example the 'wsdl' transport is a generic
     * 'finder' transport that will use Axis, Xfire or Glue to create the WSDL
     * client. These transport protocols would be wsdl-axis, wsdl-xfire and
     * wsdl-glue, but they can all support 'wsdl' protocol too.
     *
     * @param protocol the supported protocol to register
     */
    protected void registerSupportedProtocolWithoutPrefix(String protocol)
    {
        supportedProtocols.add(protocol.toLowerCase());
    }

    public void unregisterSupportedProtocol(String protocol)
    {
        protocol = protocol.toLowerCase();
        if (protocol.startsWith(getProtocol().toLowerCase()))
        {
            supportedProtocols.remove(protocol);
        }
        else
        {
            supportedProtocols.remove(getProtocol().toLowerCase() + ":" + protocol);
        }
    }

    /**
     * @return true if the protocol is supported by this connector.
     */
    public boolean supportsProtocol(String protocol)
    {
        return supportedProtocols.contains(protocol.toLowerCase());
    }

    /**
     * Returns an unmodifiable list of the protocols supported by this connector
     *
     * @return an unmodifiable list of the protocols supported by this connector
     */
    public List getSupportedProtocols()
    {
        return Collections.unmodifiableList(supportedProtocols);
    }

    /**
     * Sets A list of protocols that the connector can accept
     *
     * @param supportedProtocols
     */
    public void setSupportedProtocols(List supportedProtocols)
    {
        for (Iterator iterator = supportedProtocols.iterator(); iterator.hasNext();)
        {
            String s = (String) iterator.next();
            registerSupportedProtocol(s);
        }
    }

    /**
     * Returns a work manager for message receivers.
     */
    protected WorkManager getReceiverWorkManager(String receiverName) throws MuleException
    {
        return (WorkManager) receiverWorkManager.get();
    }

    /**
     * Returns a work manager for message dispatchers.
     *
     * @throws MuleException in case of error
     */
    protected WorkManager getDispatcherWorkManager() throws MuleException
    {
        return (WorkManager) dispatcherWorkManager.get();
    }

    /**
     * Returns a work manager for message requesters.
     *
     * @throws MuleException in case of error
     */
    protected WorkManager getRequesterWorkManager() throws MuleException
    {
        return (WorkManager) requesterWorkManager.get();
    }

    /**
     * Returns a Scheduler service for periodic tasks, currently limited to internal
     * use. Note: getScheduler() currently conflicts with the same method in the
     * Quartz transport
     */
    public ScheduledExecutorService getScheduler()
    {
        if (scheduler.get() == null)
        {
            ThreadFactory threadFactory = new NamedThreadFactory(this.getName() + ".scheduler");
            ScheduledThreadPoolExecutor newExecutor = new ScheduledThreadPoolExecutor(4, threadFactory);
            newExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            newExecutor.setKeepAliveTime(this.getReceiverThreadingProfile().getThreadTTL(),
                TimeUnit.MILLISECONDS);
            newExecutor.allowCoreThreadTimeOut(true);

            if (!scheduler.compareAndSet(null, newExecutor))
            {
                // someone else was faster, ditch our copy.
                newExecutor.shutdown();
            }
        }

        return (ScheduledExecutorService) scheduler.get();
    }

    /**
     * Getter for property 'sessionHandler'.
     *
     * @return Value for property 'sessionHandler'.
     */
    public SessionHandler getSessionHandler()
    {
        return sessionHandler;
    }

    /**
     * Setter for property 'sessionHandler'.
     *
     * @param sessionHandler Value to set for property 'sessionHandler'.
     */
    public void setSessionHandler(SessionHandler sessionHandler)
    {
        this.sessionHandler = sessionHandler;
    }

    public void workAccepted(WorkEvent event)
    {
        this.handleWorkException(event, "workAccepted");
    }

    public void workRejected(WorkEvent event)
    {
        this.handleWorkException(event, "workRejected");
    }

    public void workStarted(WorkEvent event)
    {
        this.handleWorkException(event, "workStarted");
    }

    public void workCompleted(WorkEvent event)
    {
        this.handleWorkException(event, "workCompleted");
    }

    protected void handleWorkException(WorkEvent event, String type)
    {
        if (event == null)
        {
            return;
        }

        Throwable e = event.getException();

        if (e == null)
        {
            return;
        }

        if (e.getCause() != null)
        {
            e = e.getCause();
        }

        logger.error("Work caused exception on '" + type + "'. Work being executed was: "
                        + event.getWork().toString());

        if (e instanceof Exception)
        {
            this.handleException((Exception) e);
        }
        else
        {
            throw new MuleRuntimeException(CoreMessages.connectorCausedError(this.getName()), e);
        }
    }

    // TODO the following methods should probably be lifecycle-enabled;
    // for now they are only stubs to get the refactoring going.

    public void dispatch(OutboundEndpoint endpoint, MuleEvent event) throws DispatchException
    {
        MessageDispatcher dispatcher = null;

        try
        {
            dispatcher = this.getDispatcher(endpoint);
            dispatcher.dispatch(event);
        }
        catch (DispatchException dex)
        {
            throw dex;
        }
        catch (MuleException ex)
        {
            throw new DispatchException(event.getMessage(), endpoint, ex);
        }
        finally
        {
            this.returnDispatcher(endpoint, dispatcher);
        }
    }

    /**
     * This method will return the dispatcher to the pool or, if the payload is an inputstream,
     * replace the payload with a new DelegatingInputStream which returns the dispatcher to
     * the pool when the stream is closed.
     *
     * @param endpoint
     * @param dispatcher
     * @param result
     */
    protected void setupDispatchReturn(final OutboundEndpoint endpoint,
                                       final MessageDispatcher dispatcher,
                                       MuleMessage result)
    {
        if (result != null && result.getPayload() instanceof InputStream)
        {
            DelegatingInputStream is = new DelegatingInputStream((InputStream)result.getPayload())
            {
                public void close() throws IOException
                {
                    try
                    {
                        super.close();
                    }
                    finally
                    {
                        returnDispatcher(endpoint, dispatcher);
                    }
                }
            };
            result.setPayload(is);
        }
        else
        {

            this.returnDispatcher(endpoint, dispatcher);
        }
    }

    public MuleMessage request(String uri, long timeout) throws Exception
    {
        return request(getMuleContext().getRegistry().lookupEndpointFactory().getInboundEndpoint(uri),
                timeout);
    }

    public MuleMessage request(InboundEndpoint endpoint, long timeout) throws Exception
    {
        MessageRequester requester = null;
        MuleMessage result = null;
        try
        {
            requester = this.getRequester(endpoint);
            result = requester.request(timeout);
            return result;
        }
        finally
        {
            setupRequestReturn(endpoint, requester, result);
        }
    }

    /**
     * This method will return the requester to the pool or, if the payload is an inputstream,
     * replace the payload with a new DelegatingInputStream which returns the requester to
     * the pool when the stream is closed.
     *
     * @param endpoint
     * @param requester
     * @param result
     */
    protected void setupRequestReturn(final InboundEndpoint endpoint,
                                      final MessageRequester requester,
                                      MuleMessage result)
    {
        if (result != null && result.getPayload() instanceof InputStream)
        {
            DelegatingInputStream is = new DelegatingInputStream((InputStream)result.getPayload())
            {
                public void close() throws IOException
                {
                    try
                    {
                        super.close();
                    }
                    finally
                    {
                        returnRequester(endpoint, requester);
                    }
                }
            };
            result.setPayload(is);
        }
        else
        {

            this.returnRequester(endpoint, requester);
        }
    }

    public MuleMessage send(OutboundEndpoint endpoint, MuleEvent event) throws DispatchException
    {
        MessageDispatcher dispatcher = null;

        try
        {
            dispatcher = this.getDispatcher(endpoint);
            return dispatcher.send(event);
        }
        catch (DispatchException dex)
        {
            throw dex;
        }
        catch (MuleException ex)
        {
            throw new DispatchException(event.getMessage(), endpoint, ex);
        }
        finally
        {
            this.returnDispatcher(endpoint, dispatcher);
        }
    }

    // -------- Methods from the removed AbstractServiceEnabled Connector

    /**
     * When this connector is created via the
     * {@link org.mule.transport.service.TransportFactory} the endpoint used to
     * determine the connector type is passed to this method so that any properties
     * set on the endpoint that can be used to initialise the connector are made
     * available.
     *
     * @param endpointUri the {@link EndpointURI} use to create this connector
     * @throws InitialisationException If there are any problems with the
     *             configuration set on the Endpoint or if another exception is
     *             thrown it is wrapped in an InitialisationException.
     */
    public void initialiseFromUrl(EndpointURI endpointUri) throws InitialisationException
    {
        if (!supportsProtocol(endpointUri.getFullScheme()))
        {
            throw new InitialisationException(
                CoreMessages.schemeNotCompatibleWithConnector(endpointUri.getFullScheme(),
                    this.getClass()), this);
        }
        Properties props = new Properties();
        props.putAll(endpointUri.getParams());
        // auto set username and password
        if (endpointUri.getUserInfo() != null)
        {
            props.setProperty("username", endpointUri.getUser());
            String passwd = endpointUri.getPassword();
            if (passwd != null)
            {
                props.setProperty("password", passwd);
            }
        }
        String host = endpointUri.getHost();
        if (host != null)
        {
            props.setProperty("hostname", host);
            props.setProperty("host", host);
        }
        if (endpointUri.getPort() > -1)
        {
            props.setProperty("port", String.valueOf(endpointUri.getPort()));
        }

        org.mule.util.BeanUtils.populateWithoutFail(this, props, true);

        setName(ObjectNameHelper.getConnectorName(this));
        //initialise();
    }

    /**
     * Initialises this connector from its {@link TransportServiceDescriptor} This
     * will be called before the {@link #doInitialise()} method is called.
     *
     * @throws InitialisationException InitialisationException If there are any
     *             problems with the configuration or if another exception is thrown
     *             it is wrapped in an InitialisationException.
     */
    protected synchronized void initFromServiceDescriptor() throws InitialisationException
    {
        try
        {
            serviceDescriptor = (TransportServiceDescriptor)
                RegistryContext.getRegistry().lookupServiceDescriptor(ServiceDescriptorFactory.PROVIDER_SERVICE_TYPE, getProtocol().toLowerCase(), serviceOverrides);
            if (serviceDescriptor == null)
            {
                throw new ServiceException(CoreMessages.noServiceTransportDescriptor(getProtocol()));
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Loading DispatcherFactory for connector: " + getName() + " (" + getClass().getName() + ")");
            }

            MessageDispatcherFactory df = serviceDescriptor.createDispatcherFactory();
            if (df != null)
            {
                this.setDispatcherFactory(df);
            }
            else if (logger.isDebugEnabled())
            {
                logger.debug("Transport '" + getProtocol() + "' will not support outbound endpoints: ");
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Loading RequesterFactory for connector: " + getName() + " (" + getClass().getName() + ")");
            }

            MessageRequesterFactory rf = serviceDescriptor.createRequesterFactory();
            if (rf != null)
            {
                this.setRequesterFactory(rf);
            }
            else if (logger.isDebugEnabled())
            {
                logger.debug("Transport '" + getProtocol() + "' will not support requests: ");
            }


            sessionHandler = serviceDescriptor.createSessionHandler();

            // TODO Do we still need to support this for 2.x?
            // Set any manager default properties for the connector. These are set on
            // the Manager with a protocol e.g. jms.specification=1.1
            // This provides a really convenient way to set properties on an object
            // from unit tests
//            Map props = new HashMap();
//            PropertiesUtils.getPropertiesWithPrefix(muleContext.getRegistry().lookupProperties(), getProtocol()
//                .toLowerCase(), props);
//            if (props.size() > 0)
//            {
//                props = PropertiesUtils.removeNamespaces(props);
//                org.mule.util.BeanUtils.populateWithoutFail(this, props, true);
//            }
        }
        catch (Exception e)
        {
            throw new InitialisationException(e, this);
        }
    }

    /**
     * Get the {@link TransportServiceDescriptor} for this connector. This will be
     * null if the connector was created by the developer. To create a connector the
     * proper way the developer should use the {@link TransportFactory} and pass in
     * an endpoint.
     *
     * @return the {@link TransportServiceDescriptor} for this connector
     */
    protected TransportServiceDescriptor getServiceDescriptor()
    {
        if (serviceDescriptor == null)
        {
            throw new IllegalStateException("This connector has not yet been initialised: " + name);
        }
        return serviceDescriptor;
    }

    /**
     * Create a Message receiver for this connector
     *
     * @param service the service that will receive events from this receiver,
     *            the listener
     * @param endpoint the endpoint that defies this inbound communication
     * @return an instance of the message receiver defined in this connectors'
     *         {@link org.mule.transport.service.TransportServiceDescriptor}
     *         initialised using the service and endpoint.
     * @throws Exception if there is a problem creating the receiver. This exception
     *             really depends on the underlying transport, thus any exception
     *             could be thrown
     */
    protected MessageReceiver createReceiver(Service service, InboundEndpoint endpoint)
        throws Exception
    {
        return getServiceDescriptor().createMessageReceiver(this, service, endpoint);
    }

    /**
     * Gets a <code>MessageAdapter</code> for the endpoint for the given message
     * (data)
     *
     * @param message the data with which to initialise the
     *            <code>MessageAdapter</code>
     * @return the <code>MessageAdapter</code> for the endpoint
     * @throws org.mule.api.MessagingException if the message parameter is not
     *             supported
     * @see org.mule.api.transport.MessageAdapter
     */
    public MessageAdapter getMessageAdapter(Object message) throws MessagingException
    {
        try
        {
            return serviceDescriptor.createMessageAdapter(message);
        }
        catch (TransportServiceException e)
        {
            throw new MessagingException(CoreMessages.failedToCreate("Message Adapter"),
                message, e);
        }
    }

    /**
     * A map of fully qualified class names that should override those in the
     * connectors' service descriptor This map will be null if there are no overrides
     *
     * @return a map of override values or null
     */
    public Map getServiceOverrides()
    {
        return serviceOverrides;
    }

    /**
     * Set the Service overrides on this connector.
     *
     * @param serviceOverrides the override values to use
     */
    public void setServiceOverrides(Map serviceOverrides)
    {
        this.serviceOverrides = new Properties();
        this.serviceOverrides.putAll(serviceOverrides);
    }

    /**
     * Will get the output stream for this type of transport. Typically this
     * will be called only when Streaming is being used on an outbound endpoint.
     * If Streaming is not supported by this transport an {@link UnsupportedOperationException}
     * is thrown.   Note that the stream MUST release resources on close.  For help doing so, see
     * {@link org.mule.model.streaming.CallbackOutputStream}.
     *
     * @param endpoint the endpoint that releates to this Dispatcher
     * @param message the current message being processed
     * @return the output stream to use for this request
     * @throws MuleException in case of any error
     */
    public OutputStream getOutputStream(OutboundEndpoint endpoint, MuleMessage message)
        throws MuleException
    {
        throw new UnsupportedOperationException(
            CoreMessages.streamingNotSupported(this.getProtocol()).toString());
    }

    public MuleContext getMuleContext()
    {
        return muleContext;
    }

    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
        updateCachedNotificationHandler();
    }

    // @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer(120);
        sb.append(ClassUtils.getSimpleName(this.getClass()));
        sb.append("{this=").append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(", started=").append(started);
        sb.append(", initialised=").append(initialised);
        sb.append(", name='").append(name).append('\'');
        sb.append(", disposed=").append(disposed);
        sb.append(", numberOfConcurrentTransactedReceivers=").append(numberOfConcurrentTransactedReceivers);
        sb.append(", createMultipleTransactedReceivers=").append(createMultipleTransactedReceivers);
        sb.append(", connected=").append(connected);
        sb.append(", supportedProtocols=").append(supportedProtocols);
        sb.append(", serviceOverrides=").append(serviceOverrides);
        sb.append('}');
        return sb.toString();
    }
}
