/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.component;

import org.mule.api.AbstractMuleException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.NamedObject;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.InitialisationCallback;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.model.EntryPointResolverSet;
import org.mule.api.model.Model;
import org.mule.api.routing.InboundRouterCollection;
import org.mule.api.routing.NestedRouterCollection;
import org.mule.api.routing.OutboundRouterCollection;
import org.mule.api.routing.ResponseRouterCollection;
import org.mule.util.object.ObjectFactory;

import java.beans.ExceptionListener;
import java.io.Serializable;
import java.util.Map;

/**
 * <code>Component</code> is the interal repesentation of a Mule Managed
 * component. It is responsible for managing the interaction of events to and from
 * the component as well as managing pooled resources.
 */

public interface Component extends Serializable, Lifecycle, MuleContextAware, NamedObject
{
    /**
     * Makes an asynhronous event call to the component.
     * 
     * @param event the event to consume
     * @throws AbstractMuleException if the event fails to be processed
     */
    void dispatchEvent(MuleEvent event) throws AbstractMuleException;

    /**
     * Makes a synhronous event call to the component. This event will be consumed by
     * the component and a result returned.
     * 
     * @param event the event to consume
     * @return a MuleMessage containing the resulting message and properties
     * @throws AbstractMuleException if the event fails to be processed
     */
    MuleMessage sendEvent(MuleEvent event) throws AbstractMuleException;

    /**
     * Determines whether this component has been started
     * 
     * @return true is the component is started andready to receive events
     */
    boolean isStarted();

    /**
     * Pauses event processing for a single Mule Component. Unlike stop(), a paused
     * component will still consume messages from the underlying transport, but those
     * messages will be queued until the component is resumed.
     */
    void pause() throws AbstractMuleException;

    /**
     * Resumes a single Mule Component that has been paused. If the component is not
     * paused nothing is executed.
     */
    void resume() throws AbstractMuleException;

    /**
     * True if the component is in a paused state, false otherwise
     * 
     * @return True if the component is in a paused state, false otherwise
     */
    boolean isPaused();
    
    /**
     * The exception strategy to use to handle exceptions in the Mule UMO.
     *
     * @return the exception strategy to use. If none has been set a default will be
     *         used.
     */
    ExceptionListener getExceptionListener();

    /**
     * @return Factory which creates an instance of the actual service object.
     */
    ObjectFactory getServiceFactory();

    /**
     * Factory which creates an instance of the actual service object.
     */
    void setServiceFactory(ObjectFactory factory);

    /**
     * Inbound Routers control how events are received by a component. If no router
     * is set. A default will be used that uses the inboundProvider set on his
     * descriptor.
     *
     * @return the inbound router for this component. This will always return a valid
     *         router.
     * @see InboundRouterCollection
     */
    InboundRouterCollection getInboundRouter();

    /**
     * Outbound Routers control how events are published by a component once. the
     * event has been processed. If no router is set. A default will be used that
     * uses the outboundProvider set on his descriptor to route the event.
     *
     * @return the outbound router for this component
     * @see OutboundRouterCollection
     */
    OutboundRouterCollection getOutboundRouter();

    NestedRouterCollection getNestedRouter();

    /**
     * Response Routers control how events are returned in a request/response call.
     * It cn be use to aggregate response events before returning, thus acting as a
     * Join in a forked process. This can be used to make request/response calls a
     * lot more efficient as independent tasks can be forked, execute concurrently
     * and then join before the request completes
     *
     * @return the response router for this component
     * @see ResponseRouterCollection
     */
    ResponseRouterCollection getResponseRouter();

    /**
     * Returns the initial state of this component
     *
     * @return the initial state of this component
     */
    String getInitialState();

    /**
     * Returns the name of the model that this descriptor is registered with.
     * @return the name of the model that this descriptor is registered with or null
     * if this descriptor has not been registered with a model yet
     */
    Model getModel();

    /**
     * The exception strategy to use to handle exceptions in the Mule UMO.
     *
     * @param listener the exception strategy to use. If none has been set or
     *            argument is null a default
     */
    void setExceptionListener(ExceptionListener listener);

    /**
     * @param Factory which creates an instance of the actual service object.
     */
    //void setServiceFactory(ObjectFactory serviceFactory);

    /**
     * Inbound Routers control how events are received by a component. If no router
     * is set. A default will be used that uses the inboundProvider set on his
     * descriptor.
     *
     * @param router the inbound router for this component
     * @see InboundRouterCollection
     */
    void setInboundRouter(InboundRouterCollection router);

    /**
     * Outbound Routers control how events are published by a component once. the
     * event has been processed. If no router is set. A default will be used that
     * uses the outboundProvider set on his descriptor to route the event.
     *
     * @param router the outbound router for this component
     * @see OutboundRouterCollection
     */
    void setOutboundRouter(OutboundRouterCollection router);

    void setNestedRouter(NestedRouterCollection router);

    /**
     * Response Routers control how events are returned in a request/response call.
     * It cn be use to aggregate response events before returning, thus acting as a
     * Join in a forked process. This can be used to make request/response calls a
     * lot more efficient as independent tasks can be forked, execute concurrently
     * and then join before the request completes
     *
     * @param router the response router for this component
     * @see org.mule.api.routing.ResponseRouterCollection
     */
    void setResponseRouter(ResponseRouterCollection router);

    /**
     * Sets the initial state of this component
     *
     * @param state the initial state of this component
     * @see org.mule.ImmutableMuleDescriptor#INITIAL_STATE_STARTED
     * @see org.mule.ImmutableMuleDescriptor#INITIAL_STATE_STOPPED
     * @see org.mule.ImmutableMuleDescriptor#INITIAL_STATE_PAUSED
     */
    void setInitialState(String state);

    /**
     * Sets the Model name that this descriptor is registered within.
     * @param modelName name of the model
     */
    void setModel(Model model);

    /**
     * Register a custom initialiser
     */
    void addInitialisationCallback(InitialisationCallback callback);

    /**
     * The properties for the Mule UMO.
     * 
     * @deprecated MULE-1933 Properties for the underlying service should be set on the ServiceFactory instead.
     */
    Map getProperties();

    /**
     * The properties for the Mule UMO.
     * 
     * @deprecated MULE-1933 Properties for the underlying service should be set on the ServiceFactory instead.
     */
    void setProperties(Map properties);

    /**
     * A descriptor can have a custom entrypoint resolver for its own object.
     * By default this is null. When set this resolver will override the resolver on the model
     *
     * @return Null is a resolver set has not been set otherwise the resolver to use
     *         on this component
     */
    EntryPointResolverSet getEntryPointResolverSet();

    /**
     * A descriptor can have a custom entrypoint resolver for its own object.
     * By default this is null. When set this resolver will override the resolver on the model
     *
     * @param resolverSet theresolver set to use when resolving entry points
     *                    on this component
     */
    void setEntryPointResolverSet(EntryPointResolverSet resolverSet);
}
