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

import org.mule.api.MuleException;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.MessageDispatcher;
import org.mule.api.transport.MessageDispatcherFactory;
import org.mule.util.ClassUtils;

/**
 * <code>AbstractMessageDispatcherFactory</code> is a base implementation of the
 * <code>MessageDispatcherFactory</code> interface for managing the lifecycle of
 * message dispatchers.
 * 
 * @see MessageDispatcherFactory
 */
public abstract class AbstractMessageDispatcherFactory implements MessageDispatcherFactory
{

    public AbstractMessageDispatcherFactory()
    {
        super();
    }

    /**
     * This default implementation of
     * {@link MessageDispatcherFactory#isCreateDispatcherPerRequest()} returns
     * <code>false</code>, which means that dispatchers are pooled according to
     * their lifecycle as described in {@link MessageDispatcher}.
     * 
     * @return <code>false</code> by default, unless overwritten by a subclass.
     */
    public boolean isCreateDispatcherPerRequest()
    {
        return false;
    }

    public abstract MessageDispatcher create(OutboundEndpoint endpoint) throws MuleException;

    public void activate(OutboundEndpoint endpoint, MessageDispatcher dispatcher) throws MuleException
    {
        dispatcher.activate();
    }

    public void destroy(OutboundEndpoint endpoint, MessageDispatcher dispatcher)
    {
        dispatcher.dispose();
    }

    public void passivate(OutboundEndpoint endpoint, MessageDispatcher dispatcher)
    {
        dispatcher.passivate();
    }

    public boolean validate(OutboundEndpoint endpoint, MessageDispatcher dispatcher)
    {
        // Unless dispatchers are to be disposed of after every request, we check if
        // the dispatcher is still valid or has e.g. disposed itself after an
        // exception.
        return (this.isCreateDispatcherPerRequest() ? false : dispatcher.validate());
    }

    // @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer(60);
        sb.append(ClassUtils.getSimpleName(this.getClass()));
        sb.append("{this=").append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(", createDispatcherPerRequest=").append(this.isCreateDispatcherPerRequest());
        sb.append('}');
        return sb.toString();
    }

}
