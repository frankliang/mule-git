/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.vm.issues;

import org.mule.MuleManager;
import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleMessage;
import org.mule.tck.FunctionalTestCase;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.umo.UMOEventContext;
import org.mule.umo.model.UMOModel;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

public class MulticastRouterMule2112TestCase  extends FunctionalTestCase
{

    protected String getConfigResources()
    {
        return "multicast-router-mule-2112-test.xml";
    }

    public void testMulticastRoutingOverTwoEndpoints() throws Exception
    {
        UMOModel model = (UMOModel) MuleManager.getInstance().getModels().get("main");
        FunctionalTestComponent hop1 = (FunctionalTestComponent) model.getComponent("hop1").getInstance();
        FunctionalTestComponent hop2 = (FunctionalTestComponent) model.getComponent("hop2").getInstance();
        assertNotNull(hop1);
        assertNotNull(hop2);

        final AtomicBoolean hop1made = new AtomicBoolean(false);
        EventCallback callback1 = new EventCallback()
        {
            public void eventReceived(final UMOEventContext context, final Object component) throws Exception
            {
                assertTrue(hop1made.compareAndSet(false, true));
            }
        };

        final AtomicBoolean hop2made = new AtomicBoolean(false);
        EventCallback callback2 = new EventCallback()
        {
            public void eventReceived(final UMOEventContext context, final Object component) throws Exception
            {
                assertTrue(hop2made.compareAndSet(false, true));
            }
        };

        hop1.setEventCallback(callback1);
        hop2.setEventCallback(callback2);

        MuleClient client = new MuleClient();
        MuleMessage request = new MuleMessage("payload");
        client.dispatch("vm://inbound", request);
        Thread.sleep(1000);

        assertTrue("First callback never fired", hop1made.get());
        assertTrue("Second callback never fired", hop2made.get());
    }

}
