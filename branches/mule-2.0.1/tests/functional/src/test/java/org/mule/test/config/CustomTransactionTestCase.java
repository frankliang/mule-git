/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.config;

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.transaction.TransactionConfig;
import org.mule.tck.FunctionalTestCase;
import org.mule.tck.testmodels.mule.TestTransactionFactory;

public class CustomTransactionTestCase extends FunctionalTestCase
{
    protected String getConfigResources()
    {
        return "org/mule/test/config/custom-transaction-config.xml";
    }

    public void testConfig() throws Exception
    {
        EndpointBuilder epb = muleContext.getRegistry().lookupEndpointBuilder("testEndpoint1");
        assertNotNull(epb);
        InboundEndpoint iep = epb.buildInboundEndpoint();

        assertNotNull(iep.getTransactionConfig());
        assertTrue(iep.getTransactionConfig().getFactory() instanceof TestTransactionFactory);
        assertEquals(TransactionConfig.ACTION_ALWAYS_BEGIN, iep.getTransactionConfig().getAction());
        assertEquals(4004, iep.getTransactionConfig().getTimeout());
    }
}
