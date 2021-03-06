/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.routing.inbound;

import org.mule.api.endpoint.InboundEndpoint;
import org.mule.tck.AbstractMuleTestCase;

import java.util.ArrayList;
import java.util.List;

public class InboundRouterCollectionTestCase extends AbstractMuleTestCase
{
    public void testAddGoodEndpoint() throws Exception
    {
        DefaultInboundRouterCollection router = new DefaultInboundRouterCollection();
        InboundEndpoint endpoint = getTestInboundEndpoint("test");
        router.addEndpoint(endpoint);
        assertNotNull(router.getEndpoints());
        assertTrue(router.getEndpoints().contains(endpoint));
    }

    public void testSetGoodEndpoints() throws Exception
    {
        List<InboundEndpoint> list = new ArrayList<InboundEndpoint>();
        list.add(getTestInboundEndpoint("test"));
        list.add(getTestInboundEndpoint("test"));
        DefaultInboundRouterCollection router = new DefaultInboundRouterCollection();
        assertNotNull(router.getEndpoints());
        assertEquals(0, router.getEndpoints().size());
        router.addEndpoint(getTestInboundEndpoint("test"));
        assertEquals(1, router.getEndpoints().size());
        router.setEndpoints(list);
        assertNotNull(router.getEndpoints());
        assertEquals(2, router.getEndpoints().size());
    }
}
