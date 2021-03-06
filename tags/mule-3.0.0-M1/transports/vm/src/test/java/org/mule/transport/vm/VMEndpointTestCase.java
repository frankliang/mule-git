/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.vm;

import org.mule.api.endpoint.EndpointURI;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.vm.VMConnector;

public class VMEndpointTestCase extends AbstractMuleTestCase
{
    public void testUrlWithProvider() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("vm://some.queue?endpointName=vmProvider");
        url.initialise();
        assertEquals(VMConnector.VM, url.getScheme());
        assertEquals("some.queue", url.getAddress());
        assertEquals("vmProvider", url.getEndpointName());
        assertEquals("vm://some.queue?endpointName=vmProvider", url.toString());
        assertEquals(1, url.getParams().size());
    }

    public void testVMQUri() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("vmq://some.queue");
        url.initialise();
        assertEquals(VMQueueConnector.PROTOCOL, url.getScheme());
        assertEquals("some.queue", url.getAddress());
        assertNull(url.getEndpointName());
        assertEquals("vmq://some.queue", url.toString());
        assertEquals(0, url.getParams().size());
    }
}
