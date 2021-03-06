/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.model;

import org.mule.impl.DefaultComponentExceptionStrategy;
import org.mule.impl.DefaultLifecycleAdapterFactory;
import org.mule.impl.ImmutableMuleDescriptor;
import org.mule.impl.internal.notifications.ModelNotification;
import org.mule.impl.model.resolvers.LegacyEntryPointResolverSet;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOManagementContext;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.UMOLifecycleAdapterFactory;
import org.mule.umo.manager.UMOServerNotification;
import org.mule.umo.model.UMOEntryPointResolverSet;
import org.mule.umo.model.UMOModel;

import java.beans.ExceptionListener;
import java.util.Collection;
import java.util.Iterator;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>MuleModel</code> is the default implementation of the UMOModel. The model
 * encapsulates and manages the runtime behaviour of a Mule Server instance. It is
 * responsible for maintaining the UMOs instances and their configuration.
 */
public abstract class AbstractModel implements UMOModel
{
    public static final String DEFAULT_MODEL_NAME = "main";
    /** logger used by this class */
    protected transient Log logger = LogFactory.getLog(getClass());

    private String name = DEFAULT_MODEL_NAME;
    private UMOEntryPointResolverSet entryPointResolverSet = new LegacyEntryPointResolverSet();
    private UMOLifecycleAdapterFactory lifecycleAdapterFactory = new DefaultLifecycleAdapterFactory();

    //private Map components = new ConcurrentSkipListMap();

    protected UMOManagementContext managementContext;

    /** Collection for mule descriptors registered in this Manager */
    //protected Map descriptors = new ConcurrentHashMap();

    private AtomicBoolean initialised = new AtomicBoolean(false);

    private AtomicBoolean started = new AtomicBoolean(false);

    private ExceptionListener exceptionListener = new DefaultComponentExceptionStrategy();

    /** Default constructor */
    public AbstractModel()
    {
        // Always set default entrypoint resolver, lifecycle and compoenent
        // resolver and exceptionstrategy.
        entryPointResolverSet = new LegacyEntryPointResolverSet();
        lifecycleAdapterFactory = new DefaultLifecycleAdapterFactory();
        //components = new ConcurrentSkipListMap();
        //descriptors = new ConcurrentHashMap();
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
    public UMOEntryPointResolverSet getEntryPointResolverSet()
    {
        return entryPointResolverSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.model.UMOModel#setEntryPointResolver(org.mule.umo.model.UMOEntryPointResolver)
     */
    public void setEntryPointResolverSet(UMOEntryPointResolverSet entryPointResolverSet)
    {
        this.entryPointResolverSet = entryPointResolverSet;
    }

//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.mule.umo.UMOModel#isUMORegistered(java.lang.String)
//     */
//    public boolean isComponentRegistered(String name)
//    {
//        return (components.get(name) != null);
//    }
//    
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.mule.umo.UMOModel#registerUMO(org.mule.umo.UMODescriptor)
//     */
//    public void registerComponent(UMODescriptor descriptor) throws UMOException
//    {
//        if (descriptor == null)
//        {
//            throw new ModelException(CoreMessages.objectIsNull("UMO Descriptor"));
//        }
//
//        if (descriptor.getModelName() == null)
//        {
//            descriptor.setModelName(getName());
//        }
//        else if (!descriptor.getModelName().equals(getName()))
//        {
//            throw new ModelException(CoreMessages.modelNameDoesNotMatchModel(descriptor, getName()));
//        }
//
//        // Set the es if one wasn't set in the configuration
//        if (descriptor.getExceptionListener() == null)
//        {
//            descriptor.setExceptionListener(exceptionListener);
//        }
//
//        // detect duplicate descriptor declarations
//        if (descriptors.get(descriptor.getName()) != null)
//        {
//            throw new ModelException(CoreMessages.descriptorAlreadyExists(descriptor.getName()));
//        }
//
//        UMOComponent component = (UMOComponent) components.get(descriptor.getName());
//
//        if (component == null)
//        {
//            component = createComponent(descriptor);
//            component.setManagementContext(managementContext);
//            descriptors.put(descriptor.getName(), descriptor);
//            components.put(descriptor.getName(), component);
//        }
//
//        logger.debug("Added Mule UMO: " + descriptor.getName());
//
//        if (initialised.get())
//        {
//            logger.info("Initialising component: " + descriptor.getName());
//            component.initialise();
//        }
//        if (started.get())
//        {
//            startComponent(descriptor.getName());
//        }
//    }
//
//
//    public void unregisterComponent(UMODescriptor descriptor) throws UMOException
//    {
//        if (descriptor == null)
//        {
//            throw new ModelException(CoreMessages.objectIsNull("UMO Descriptor"));
//        }
//
//        if (!isComponentRegistered(descriptor.getName()))
//        {
//            throw new ModelException(CoreMessages.componentNotRegistered(descriptor.getName()));
//        }
//        UMOComponent component = (UMOComponent) components.remove(descriptor.getName());
//
//        if (component != null)
//        {
//            component.stop();
//            descriptors.remove(descriptor.getName());
//            component.dispose();
//            logger.info("The component: " + descriptor.getName() + " has been unregistered and disposing");
//        }
//    }

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

    /** Destroys any current components */
    public void dispose()
    {
        fireNotification(new ModelNotification(this, ModelNotification.MODEL_DISPOSING));

//        Collection components = managementContext.getRegistry().lookupComponents(getName());
//        UMOComponent component;
//        for (Iterator i = components.iterator(); i.hasNext();)
//        {
//            component = (UMOComponent) i.next();
//            try
//            {
//                component.dispose();
//                logger.info(component + " has been destroyed successfully");
//            }
//            catch (Exception e1)
//            {
//                // TODO MULE-863: So what do we do about this?
//                logger.warn("Failed to dispose component: " + e1.getMessage());
//            }
//        }

        //components.clear();
        //descriptors.clear();

        fireNotification(new ModelNotification(this, ModelNotification.MODEL_DISPOSED));
    }

//    /**
//     * Returns a valid component for the given Mule name
//     *
//     * @param muleName the Name of the Mule for which the component is required
//     * @return a component for the specified name
//     */
//    public UMOSession getComponentSession(String muleName)
//    {
//        UMOComponent component = (UMOComponent) components.get(muleName);
//        if (component == null)
//        {
//            logger.warn("Component: " + muleName + " not found returning null session");
//            return null;
//        }
//        else
//        {
//            return new MuleSession(component);
//        }
//    }

    /**
     * Stops any registered components
     *
     * @throws UMOException if a Component fails tcomponent
     */
    public void stop() throws UMOException
    {
        fireNotification(new ModelNotification(this, ModelNotification.MODEL_STOPPING));
        
//        Collection components = managementContext.getRegistry().lookupComponents(getName());
//        UMOComponent component;
//        for (Iterator i = components.iterator(); i.hasNext();)
//        {
//            component = (UMOComponent) i.next();
//            stopComponent(component);
//            logger.info("Component " + component + " has been stopped successfully");
//        }
        started.set(false);

        fireNotification(new ModelNotification(this, ModelNotification.MODEL_STOPPED));
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
            throw new IllegalStateException("Not Initialised");
        }

        if (!started.get())
        {
            fireNotification(new ModelNotification(this, ModelNotification.MODEL_STARTING));

//            Collection components = managementContext.getRegistry().lookupComponents(getName());
//            UMOComponent component;
//            for (Iterator i = components.iterator(); i.hasNext();)
//            {
//                component = (UMOComponent) i.next();
//                startComponent(component);
//            }
            started.set(true);
            fireNotification(new ModelNotification(this, ModelNotification.MODEL_STARTED));
        }
        else
        {
            logger.debug("Model already started");
        }
    }

    /**
     * Starts a single Mule Component. This can be useful when stopping and starting
     * some Mule UMOs while letting others continue
     *
     * @param name the name of the Mule UMO to start
     * @throws UMOException if the MuleUMO is not registered or the component failed
     *                      to start
     */
    public void startComponent(UMOComponent component) throws UMOException
    {
        if (component.isStarted())
        {
            logger.info("Component is already started: " + component);
        }
        else if (component.getInitialState().equals(
                ImmutableMuleDescriptor.INITIAL_STATE_STARTED))
        {
            component.start();
            logger.info("Component " + component + " has been started successfully");
        }
        else if (component.getInitialState().equals(
                ImmutableMuleDescriptor.INITIAL_STATE_PAUSED))
        {
            // TODO Why should we have to cast here?
            ((AbstractComponent) component).start(true);
            logger.info("Component " + component
                    + " has been started and paused (initial state = 'paused')");
        }
        else
        {
            logger.info("Component " + component
                    + " has not been started (initial state = 'stopped')");
        }
        logger.info("Mule " + component.toString() + " has been started successfully");
    }

    /**
     * Stops a single Mule Component. This can be useful when stopping and starting
     * some Mule UMOs while letting others continue.
     *
     * @param name the name of the Mule UMO to stop
     * @throws UMOException if the MuleUMO is not registered
     */
    public void stopComponent(UMOComponent component) throws UMOException
    {
        component.stop();
        logger.info("Mule " + name + " has been stopped successfully");
    }

    /**
     * Pauses event processing for a single Mule Component. Unlike stopComponent(), a
     * paused component will still consume messages from the underlying transport,
     * but those messages will be queued until the component is resumed. <p/> In
     * order to persist these queued messages you can set the 'recoverableMode'
     * property on the Muleconfiguration to true. this causes all internal queues to
     * store their state.
     *
     * @param name the name of the Mule UMO to stop
     * @throws org.mule.umo.UMOException if the MuleUMO is not registered or the
     *                                   component failed to pause.
     * @see org.mule.config.MuleConfiguration
     */
    public void pauseComponent(UMOComponent component) throws UMOException
    {
        component.pause();
        logger.info("Mule Component " + name + " has been paused successfully");
    }

    /**
     * Resumes a single Mule Component that has been paused. If the component is not
     * paused nothing is executed.
     *
     * @param name the name of the Mule UMO to resume
     * @throws org.mule.umo.UMOException if the MuleUMO is not registered or the
     *                                   component failed to resume
     */
    public void resumeComponent(UMOComponent component) throws UMOException
    {
        component.resume();
        logger.info("Mule Component " + name + " has been resumed successfully");
    }

//    public void setServiceDescriptors(List descriptors) throws UMOException
//    {
//        for (Iterator iterator = descriptors.iterator(); iterator.hasNext();)
//        {
//            registerComponent((UMODescriptor) iterator.next());
//        }
//    }

    public void initialise() throws InitialisationException
    {
        if (!initialised.get())
        {
            fireNotification(new ModelNotification(this, ModelNotification.MODEL_INITIALISING));

            // TODO this doesn't feel right, should be injected?
//            if (exceptionListener instanceof ManagementContextAware)
//            {
//                ((ManagementContextAware) exceptionListener).setManagementContext(managementContext);
//            }
//            if (exceptionListener instanceof Initialisable)
//            {
//                ((Initialisable) exceptionListener).initialise();
//            }
//            Collection components = managementContext.getRegistry().lookupComponents(this.name);
//            UMOComponent component;
//            for (Iterator i = components.iterator(); i.hasNext();)
//            {
//                component = (UMOComponent) i.next();
//                component.initialise();
//
//                logger.info("Component " + component.getName()
//                        + " has been started successfully");
//            }
            initialised.set(true);
            fireNotification(new ModelNotification(this, ModelNotification.MODEL_INITIALISED));
        }
        else
        {
            logger.debug("Model already initialised");
        }
    }

    public ExceptionListener getExceptionListener()
    {
        return exceptionListener;
    }

    public void setExceptionListener(ExceptionListener exceptionListener)
    {
        this.exceptionListener = exceptionListener;
    }

//    public UMODescriptor getDescriptor(String name)
//    {
//        return (UMODescriptor) descriptors.get(name);
//    }

//    public UMOComponent getComponent(String name)
//    {
//        return (UMOComponent) components.get(name);
//    }
//
//    /**
//     * Gets an iterator of all component names registered in the model
//     *
//     * @return an iterator of all component names
//     */
//    public Iterator getComponentNames()
//    {
//        return components.keySet().iterator();
//    }

    void fireNotification(UMOServerNotification notification)
    {
        if (managementContext != null)
        {
            managementContext.fireNotification(notification);
        }
        else if (logger.isDebugEnabled())
        {
            logger.debug("ManagementContext is not yet available for firing notifications, ignoring event: " + notification);
        }
    }

    public void setManagementContext(UMOManagementContext context)
    {
        this.managementContext = context;
    }

//    protected abstract UMOComponent createComponent(UMODescriptor descriptor);
}
