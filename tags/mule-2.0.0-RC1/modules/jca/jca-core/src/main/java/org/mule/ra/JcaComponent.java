/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.ra;

import org.mule.MuleException;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.MessageFactory;
import org.mule.impl.RequestContext;
import org.mule.impl.model.AbstractComponent;
import org.mule.management.stats.ComponentStatistics;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOManagementContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.model.UMOEntryPoint;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * <code>JcaComponent</code> Is the type of component used in Mule when embedded
 * inside an app server using JCA. In the future we might want to use one of the
 * existing models.
 */
public class JcaComponent extends AbstractComponent implements UMOComponent
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = -1510441245219710451L;

    private transient final UMOComponent component;
    private transient UMOEntryPoint entryPoint;
    private Object pojoService;
    private ComponentStatistics stats;
    private UMOManagementContext managementContext;

    /**
     * Determines if the component has been initialised
     */
    private final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Determines if the component has been started
     */
    private final AtomicBoolean started = new AtomicBoolean(false);

    public JcaComponent(UMOComponent component)
    {
        this.component = component;
    }

    public void setManagementContext(UMOManagementContext context)
    {
        this.managementContext = context;
    }

    public UMOComponent getComponent()
    {
        return component;
    }

    public void dispatchEvent(UMOEvent event) throws UMOException
    {
        try
        {
            // Invoke method
            entryPoint.invoke(pojoService, RequestContext.getEventContext());
        }
        catch (Exception e)
        {
            throw new MuleException(
                CoreMessages.failedToInvoke("UMO Component: " + component.getName()), e);
        }
    }

    /**
     * This is the synchronous call method and not supported by components managed in
     * a JCA container
     * 
     * @param event
     * @return
     * @throws UMOException
     */
    public UMOMessage sendEvent(UMOEvent event) throws UMOException
    {
        throw new UnsupportedOperationException("sendEvent()");
    }

    public void doPause() throws UMOException
    {
        // nothing to do
    }

    public void doResume() throws UMOException
    {
        // nothing to do
    }

    public boolean isPaused()
    {
        return false;
    }

    public void start() throws UMOException
    {
        started.set(true);
    }

    public void stop() throws UMOException
    {
        started.set(false);
    }

    public void doDispose()
    {
        managementContext.getStatistics().remove(stats);
    }

    public synchronized void doInitialise() throws InitialisationException
    {
        throw new InitialisationException(MessageFactory.createStaticMessage("This component needs to be reviewed for Mule 2.0 (see MULE-1910)"), this);
        /*
        if (initialised.get())
        {
            throw new InitialisationException(
                CoreMessages.objectAlreadyInitialised("Component '" + descriptor.getName() + "'"), this);
        }
        descriptor.initialise();
        try
        {
            entryPoint = managementContext.getRegistry().lookupModel(JcaModel.JCA_MODEL_TYPE).getEntryPointResolver().resolveEntryPoint(
                descriptor);
        }
        catch (UMOException e)
        {
            throw new InitialisationException(e, this);
        }

        // initialise statistics
        stats = new ComponentStatistics(descriptor.getName(), -1);

        stats.setEnabled(managementContext.getStatistics().isEnabled());
        managementContext.getStatistics().add(stats);
        stats.setOutboundRouterStat(getDescriptor().getOutboundRouter().getStatistics());
        stats.setInboundRouterStat(getDescriptor().getInboundRouter().getStatistics());

        component = descriptor.getService();

        initialised.set(true);
        managementContext.fireNotification(
            new ComponentNotification(descriptor, ComponentNotification.COMPONENT_INITIALISED));
        */
    }

    protected Object getDelegateComponent() throws InitialisationException
    {
        throw new InitialisationException(MessageFactory.createStaticMessage("This component needs to be reviewed for Mule 2.0 (see MULE-1910)"), this);
        /*
        Object impl = descriptor.getService();
        Object component = null;

            if (impl instanceof ContainerKeyPair)
            {
                component = managementContext.getRegistry().lookupObject(((ContainerKeyPair)impl).getKey());

                if (descriptor.isSingleton())
                {
                    descriptor.setService(component);
                }
            }
            else
            {
                component = impl;
            }


        // Call any custom initialisers
        descriptor.fireInitialisationCallbacks(component);
        return component;
        */
    }

    public boolean isStarted()
    {
        return started.get();
    }

    /**
     * Gets the underlying instance form this component Where the Component
     * implmentation provides pooling this is no 1-2-1 mapping between UMOComponent
     * and instance, so this method will return the object in initial state. <p/> If
     * the underlying component is Container managed in Spring or another IoC
     * container then the object instance in the IoC container will be returned
     * 
     * @return the underlying instance form this component
     */
    public Object getInstance() throws UMOException
    {
        return pojoService;
    }

    protected void doDispatch(UMOEvent event) throws UMOException
    {
        // nothing to do?
    }

    protected UMOMessage doSend(UMOEvent event) throws UMOException
    {
        // nothing to do?
        return null;
    }
}
