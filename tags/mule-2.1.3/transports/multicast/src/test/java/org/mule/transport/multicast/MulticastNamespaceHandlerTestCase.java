/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.multicast;

import org.mule.tck.FunctionalTestCase;
import org.mule.transport.multicast.MulticastConnector;

/**
 * TODO
 */
public class MulticastNamespaceHandlerTestCase extends FunctionalTestCase
{
    protected String getConfigResources()
    {
        return "multicast-namespace-config.xml";
    }

    public void testConfig() throws Exception
    {
        MulticastConnector c = (MulticastConnector)muleContext.getRegistry().lookupConnector("multicastConnector");
        assertNotNull(c);

        assertEquals(1234, c.getReceiveBufferSize());
        assertEquals(2345, c.getReceiveTimeout());
        assertEquals(3456, c.getSendBufferSize());
        assertEquals(4567, c.getSendTimeout());
        assertEquals(5678, c.getTimeToLive());
        assertEquals(true, c.isBroadcast());
        assertEquals(true, c.isLoopback());
        assertEquals(false, c.isKeepSendSocketOpen());
        assertTrue(c.isConnected());
        assertTrue(c.isStarted());
    }

}