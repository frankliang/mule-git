/*
 * $Header: /cvsroot/mule/mule/src/java/org/mule/model/MuleModel.java,v 1.5
 * 2004/01/18 12:06:05 rossmason Exp $ $Revision$ $Date: 2004/01/18
 * 12:06:05 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) Cubis Limited. All rights reserved. http://www.cubis.co.uk
 * 
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 *  
 */
package org.mule.impl;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.InitialisationException;
import org.mule.MuleException;
import org.mule.impl.internal.events.ModelEvent;
import org.mule.impl.internal.events.ServerEventManager;
import org.mule.model.DynamicEntryPointResolver;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMODescriptor;
import org.mule.umo.UMOException;
import org.mule.umo.UMOExceptionStrategy;
import org.mule.umo.UMOServerEvent;
import org.mule.umo.UMOSession;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.UMOLifecycleAdapterFactory;
import org.mule.umo.model.ModelException;
import org.mule.umo.model.UMOEntryPointResolver;
import org.mule.umo.model.UMOModel;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * <code>MuleModel</code> is the default implementation of the UMOModel. The
 * model encapsulates and manages the runtime behaviour of a Mule Server
 * instance. It is responsible for maintaining the UMOs instances and their
 * configuration.
 * 
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class MuleModel implements UMOModel
{
    /**
     * logger used by this class
     */
    private static transient Log logger = LogFactory.getLog(MuleModel.class);

    private String name;
    private UMOEntryPointResolver entryPointResolver;
    private UMOLifecycleAdapterFactory lifecycleAdapterFactory;

    private ConcurrentHashMap components;

    /**
     * Collection for mule descriptors registered in this Manager
     */
    protected ConcurrentHashMap descriptors;

    private SynchronizedBoolean initialised = new SynchronizedBoolean(false);

    private SynchronizedBoolean started = new SynchronizedBoolean(false);

    private UMOExceptionStrategy exceptionStrategy;

    private ServerEventManager listeners;

    /**
     * Default constructor
     */
    public MuleModel()
    {
        //Always set default entrypoint resolver, lifecycle and compoenent
        // resolver and exceptionstrategy.
        entryPointResolver = new DynamicEntryPointResolver();
        lifecycleAdapterFactory = new DefaultLifecycleAdapterFactory();
        components = new ConcurrentHashMap();
        descriptors = new ConcurrentHashMap();
        exceptionStrategy = new DefaultComponentExceptionStrategy();
        name = "mule";
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOModel#getName()
	 */
    public String getName()
    {
        return name;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOModel#setName(java.lang.String)
	 */
    public void setName(String name)
    {
        this.name = name;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.model.UMOModel#getEntryPointResolver()
	 */
    public UMOEntryPointResolver getEntryPointResolver()
    {
        return entryPointResolver;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.model.UMOModel#setEntryPointResolver(org.mule.umo.model.UMOEntryPointResolver)
	 */
    public void setEntryPointResolver(UMOEntryPointResolver entryPointResolver)
    {
        this.entryPointResolver = entryPointResolver;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOModel#isUMORegistered(java.lang.String)
	 */
    public boolean isComponentRegistered(String name)
    {
        return (components.get(name) != null);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOModel#registerUMO(org.mule.umo.UMODescriptor)
	 */
    public UMOComponent registerComponent(UMODescriptor descriptor) throws UMOException
    {
        if (descriptor == null)
        {
            throw new MuleException("Mule UMO descriptor to register was null");
        }

        //Set the es if one wasn't set in the configuration
        if (descriptor.getExceptionStrategy() == null)
        {
            descriptor.setExceptionStrategy(exceptionStrategy);
        }

        UMOComponent component = (UMOComponent) components.get(descriptor.getName());

        if (component == null)
        {
            component = new MuleComponent((MuleDescriptor)descriptor);
            descriptors.put(descriptor.getName(), descriptor);
            components.put(descriptor.getName(), component);
        }

        logger.debug("Added Mule UMO: " + descriptor.getName());

        if(initialised.get()) {
            try
            {
                logger.info("Initialising component: " + descriptor.getName());
                component.initialise();
            } catch (Exception e)
            {
                throw new InitialisationException("Failed to start component: " + descriptor.getName() + ". Error is: " + e.getMessage(),e);
            }
        }
        if(started.get()) {
            logger.info("Starting component: " + descriptor.getName());
            registerListeners(component);
            component.start();
        }
        return component;
    }

    public void unregisterComponent(UMODescriptor descriptor) throws UMOException
    {
        if (descriptor == null)
        {
            throw new ModelException("Mule UMO descriptor to unregister was null");
        }

        if(!isComponentRegistered(descriptor.getName())) {
            throw new ModelException("Component not registered: " + descriptor.getName());
        }
        UMOComponent component = (UMOComponent) components.remove(descriptor.getName());

        if (component != null)
        {
            component.stop();
            unregisterListeners(component);
            descriptors.remove(descriptor.getName());
            component.dispose();
            logger.info("The component: " + descriptor.getName() + " has been unregistered and disposing");
        } else {
            throw new MuleException("component: "  + descriptor.getName() + "is not registered with this model");
        }
    }

    protected void registerListeners(UMOComponent component) throws UMOException
    {
        UMOEndpoint endpoint;
        List endpoints = new ArrayList();
        endpoints.addAll(component.getDescriptor().getInboundRouter().getEndpoints());
        if(component.getDescriptor().getInboundEndpoint()!=null) {
            endpoints.add(component.getDescriptor().getInboundEndpoint());
        }
        //Add response endpoints if any
        if(component.getDescriptor().getResponseRouter()!=null &&
                component.getDescriptor().getResponseRouter().getEndpoints()!=null) {
            endpoints.addAll(component.getDescriptor().getResponseRouter().getEndpoints());
        }

        for (Iterator it = endpoints.iterator(); it.hasNext();)
        {
            endpoint = (UMOEndpoint) it.next();
            try
            {
                endpoint.getConnector().registerListener(component, endpoint);
            } catch (Exception e)
            {
                throw new MuleException("Failed to register Component: " + component.getDescriptor().getName() +
                        " as a listener on connector: " + endpoint.getConnector().getName() + ". Error is: " + e, e);
            }
        }
    }

    protected void unregisterListeners(UMOComponent component) throws UMOException
    {
        UMOEndpoint endpoint;
        List endpoints = component.getDescriptor().getInboundRouter().getEndpoints();
        if(component.getDescriptor().getInboundEndpoint()!=null) {
            endpoints.add(component.getDescriptor().getInboundEndpoint());
        }

        for (Iterator it = endpoints.iterator(); it.hasNext();)
        {
            endpoint = (UMOEndpoint) it.next();
            try
            {
                endpoint.getConnector().unregisterListener(component, endpoint);
            } catch (Exception e)
            {
                throw new MuleException("Failed to unregister Component: " + component.getDescriptor().getName() +
                        " as a listener on connector: " + endpoint.getConnector().getName() + ". Error is: " + e, e);
            }
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.model.UMOModel#getLifecycleAdapterFactory()
	 */
    public UMOLifecycleAdapterFactory getLifecycleAdapterFactory()
    {
        return lifecycleAdapterFactory;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.model.UMOModel#setLifecycleAdapterFactory(org.mule.umo.lifecycle.UMOLifecycleAdapterFactory)
	 */
    public void setLifecycleAdapterFactory(UMOLifecycleAdapterFactory lifecycleAdapterFactory)
    {
        this.lifecycleAdapterFactory = lifecycleAdapterFactory;
    }

    /**
     * Destroys any current components
     * <p/>
     * //* @throws UMOException if the components don't destroy gracefully
     */
    public void dispose() throws UMOException
    {
        fireEvent(new ModelEvent(this, ModelEvent.MODEL_DISPOSING));
        UMOComponent temp = null;
        Object key = null;
        for (Iterator i = components.keySet().iterator(); i.hasNext();)
        {
            try
            {
                key = i.next();
                temp = (UMOComponent) components.get(key);
                try
                {
                    temp.dispose();
                } catch (Exception e1)
                {
                    logger.warn("Failed to dispose component: " + e1.getMessage());
                }
                components.remove(temp.getDescriptor());
                logger.info(temp + " has been destroyed successfully");
            } catch (ConcurrentModificationException e)
            {
                logger.warn("cannot dispose calling component");
                return;
            }
        }
        fireEvent(new ModelEvent(this, ModelEvent.MODEL_DISPOSED));        
    }

    /**
     * Returns a valid component for the given Mule name
     *
     * @param muleName the Name of the Mule for which the component is required
     * @return a component for the specified name
     */
    public UMOSession getComponentSession(String muleName)
    {
        UMOComponent component = (UMOComponent) components.get(muleName);
        if(component==null) {
            logger.warn("Component: " + muleName + " not found returning null session");
            return null;
        } else {
            return new MuleSession(component,  TransactionCoordination.getInstance().getTransaction());
        }
    }

    /**
     * Stops any registered components
     *
     * @throws UMOException if a Component fails tcomponent
     */
    public void stop() throws UMOException
    {
        fireEvent(new ModelEvent(this, ModelEvent.MODEL_STOPPING));
        UMOComponent temp = null;
        for (Iterator i = components.values().iterator(); i.hasNext();)
        {
            temp = (UMOComponent) i.next();
            try
            {
                temp.stop();
            } catch (Exception e)
            {
                throw new MuleException("Failed to stop Component: " + e.getMessage(), e);
            }

            logger.info("Component " + temp + " has been stopped successfully");
        }
        fireEvent(new ModelEvent(this, ModelEvent.MODEL_STOPPED));
    }

    /**
     * Starts all registered components
     *
     * @throws UMOException if any of the components fail to start
     */
    public void start() throws UMOException
    {
        if (!initialised.get())
        {
            try
            {
                initialise();
            } catch (Exception e)
            {
                throw new InitialisationException("Failed to initialise Model: " + e.getMessage(), e);
            }
        }

        if(!started.get()) {
            fireEvent(new ModelEvent(this, ModelEvent.MODEL_STARTING));

            UMOComponent temp = null;
            for (Iterator i = components.values().iterator(); i.hasNext();)
            {
                temp = (UMOComponent) i.next();
                try
                {
                    registerListeners(temp);
                    temp.start();
                } catch (Exception e)
                {
                    throw new MuleException("Failed to start component: " + e.getMessage(), e);
                }

                logger.info("Component " + temp + " has been started successfully");
            }
            started.set(true);
            fireEvent(new ModelEvent(this, ModelEvent.MODEL_STARTED));
        } else {
            logger.debug("Model already started");
        }
    }

    /**
     * Stops a single Mule Component. This can be useful when stopping and
     * starting some Mule UMOs while letting others continue.
     *
     * @param name the name of the Mule UMO to stop
     * @throws UMOException if the MuleUMO is not registered
     */
    public void stopComponent(String name) throws UMOException
    {
        UMOComponent component = (UMOComponent)components.get(name);
        if (component == null)
        {
            throw new ModelException("Cannot find Mule Component " + name);
        } else
        {
            try
            {
                unregisterListeners(component);
                component.stop();

            } catch (Exception e)
            {
                throw new MuleException("Failed to stop Mule session: " + name, e);
            }
            logger.info("mule " + name + " has been stopped successfully");
        }
    }

    /**
     * Starts a single Mule Component. This can be useful when stopping and
     * starting some Mule UMOs while letting others continue
     *
     * @param name the name of the Mule UMO to start
     * @throws UMOException if the MuleUMO is not registered or the component failed to start
     */
    public void startComponent(String name) throws UMOException
    {
        UMOComponent component = (UMOComponent)components.get(name);
        if (component == null)
        {
            throw new MuleException("Cannot find mule " + name);
        } else
        {
            try
            {
                registerListeners(component);
                component.start();
            } catch (Exception e)
            {
                throw new MuleException("Failed to start Mule Component: " + name, e);
            }
            logger.info("Mule " + component.toString() + " has been started successfully");
        }
    }

    /**
     * Pauses event processing for a single Mule Component.
     * Unlike stopComponent(), a paused component will still consume messages from the
     * underlying transport, but those messages will be queued until the component is
     * resumed.
     * <p/>
     * In order to persist these queued messages you can set the 'recoverableMode' property
     * on the Muleconfiguration to true.  this causes all internal queues to store their state.
     *
     * @param name the name of the Mule UMO to stop
     * @throws org.mule.umo.UMOException if the MuleUMO is not registered or the
     *                                   component failed to pause.
     * @see org.mule.config.MuleConfiguration
     */
    public void pauseComponent(String name) throws UMOException
    {
        UMOComponent component = (UMOComponent)components.get(name);
        if (component == null)
        {
            throw new MuleException("Cannot find Mule Component " + name);
        } else
        {
            try
            {
                component.pause();
            } catch (Exception e)
            {
                throw new MuleException("Failed to stop Mule session: " + name, e);
            }
            logger.info("Mule Component " + name + " has been paused successfully");
        }
    }

    /**
     * Resumes a single Mule Component that has been paused. If the component is not paused
     * nothing is executed.
     *
     * @param name the name of the Mule UMO to resume
     * @throws org.mule.umo.UMOException if the MuleUMO is not registered or the component failed to resume
     */
    public void resumeComponent(String name) throws UMOException
    {
        UMOComponent component = (UMOComponent)components.get(name);
        if (component == null)
        {
            throw new MuleException("Cannot find Mule Component " + name);
        } else
        {
            try
            {
                component.resume();
            } catch (Exception e)
            {
                throw new MuleException("Failed to stop Mule session: " + name, e);
            }
            logger.info("Mule Component " + name + " has been resumed successfully");
        }
    }

    public void setComponents(List descriptors) throws UMOException
    {

        for (Iterator iterator = descriptors.iterator(); iterator.hasNext();)
        {
            registerComponent((UMODescriptor) iterator.next());
        }
    }

    public void initialise() throws InitialisationException
    {
        if(!initialised.get()) {
            fireEvent(new ModelEvent(this, ModelEvent.MODEL_INITIALISING));

            UMOComponent temp = null;
            for (Iterator i = components.values().iterator(); i.hasNext();)
            {
                temp = (UMOComponent) i.next();
                temp.initialise();

                logger.info("Component " + temp.getDescriptor().getName() + " has been started successfully");
            }
            initialised.set(true);
            fireEvent(new ModelEvent(this, ModelEvent.MODEL_INITIALISED));
        } else {
            logger.debug("Model already initialised");
        }
    }

    public UMOExceptionStrategy getExceptionStrategy()
    {
        return exceptionStrategy;
    }

    public void setExceptionStrategy(UMOExceptionStrategy exceptionStrategy)
    {
        this.exceptionStrategy = exceptionStrategy;
    }

    public UMODescriptor getDescriptor(String name)
    {
        return (UMODescriptor)descriptors.get(name);
    }

    /**
     * Gets an iterator of all component names registered in the model
     *
     * @return an iterator of all component names
     */
    public Iterator getComponentNames()
    {
        return components.keySet().iterator();
    }

    public void setListeners(ServerEventManager listeners)
    {
        this.listeners = listeners;
    }

    void fireEvent(UMOServerEvent event) {
        if(listeners!=null) {
            listeners.fireEvent(event);
        }
    }
}
