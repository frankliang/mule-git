/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.routing;

import org.mule.api.MessagingException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleSession;
import org.mule.api.endpoint.ImmutableEndpoint;

/**
 * <code>OutboundRouterCollection</code> is responsible for holding all outbound routers for a service service.
 */

public interface OutboundRouterCollection extends RouterCollection
{
    /**
     * Prepares one or more events to be dispached by a Message Dispatcher.
     * 
     * @param message The source Message
     * @param session The current session
     * @return a list containing 0 or events to be dispatched
     * @throws RoutingException If any of the events cannot be created.
     */
    MuleMessage route(MuleMessage message, MuleSession session, boolean synchronous) throws MessagingException;

    /**
     * A helper method for finding out which endpoints a message would be routed to
     * without actually routing the the message.
     * 
     * @param message the message to retrieve endpoints for
     * @return an array of Endpoint objects or an empty array
     * @throws RoutingException if there is a filter exception
     */
    ImmutableEndpoint[] getEndpointsForMessage(MuleMessage message) throws MessagingException;

    /**
     * Determines if any endpoints have been set on this router.
     * 
     * @return
     */
    boolean hasEndpoints();
}
