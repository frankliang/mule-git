/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.soap.axis;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;

import java.util.List;
import java.util.Map;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class AxisMessageDispatcherTestCase extends AbstractMuleTestCase
{
    public void testNullParametersInCallAllowed() throws Exception
    {
        UMOImmutableEndpoint ep = managementContext.getRegistry().lookupEndpointFactory().createOutboundEndpoint(
            "axis:http://www.muleumo.org/services/myService?method=myTestMethod", managementContext);
        AxisMessageDispatcher dispatcher = new AxisMessageDispatcher(ep);
        dispatcher.service = new Service();
        UMOEvent event = getTestEvent("testPayload", ep);
        // there should be no NullPointerException
        Call call = dispatcher.getCall(event, new Object[]{null});

        assertNotNull(call);

        UMOMessage msg = event.getMessage();
        assertNotNull(msg);
        final Map soapMethods = (Map)msg.getProperty("soapMethods");
        assertEquals(1, soapMethods.size());
        final List values = (List)soapMethods.get("myTestMethod");
        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals("value0;qname{:anyType:http://www.w3.org/2001/XMLSchema};in", values.get(0));
    }
}
