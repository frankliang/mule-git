/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.umo.routing;

import org.mule.umo.MessagingException;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;

import java.util.List;

/**
 * <code>InboundRouterCollection</code> manages a collection of inbound routers.
 */

public interface UMOInboundRouterCollection extends UMORouterCollection
{
    UMOMessage route(UMOEvent event) throws MessagingException;

    void addRouter(UMOInboundRouter router);

    UMOInboundRouter removeRouter(UMOInboundRouter router);

    void addEndpoint(UMOImmutableEndpoint endpoint);

    boolean removeEndpoint(UMOImmutableEndpoint endpoint);

    List getEndpoints();

    /**
     * @param name the Endpoint identifier
     * @return the Endpoint or null if the endpointUri is not registered
     * @see UMOInboundRouterCollection
     */
    UMOImmutableEndpoint getEndpoint(String name);

    void setEndpoints(List endpoints);
}
