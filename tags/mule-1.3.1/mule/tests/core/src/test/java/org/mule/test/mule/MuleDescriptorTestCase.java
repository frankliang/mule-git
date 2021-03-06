/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.mule;

import org.mule.config.MuleConfiguration;
import org.mule.impl.MuleDescriptor;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.tck.testmodels.fruit.Orange;
import org.mule.tck.testmodels.mule.TestExceptionStrategy;
import org.mule.umo.UMODescriptor;
import org.mule.umo.endpoint.UMOEndpoint;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class MuleDescriptorTestCase extends AbstractMuleTestCase
{
    public void testDescriptorDefaults() throws Exception
    {
        MuleDescriptor descriptor = new MuleDescriptor();
        descriptor.initialise();
        MuleConfiguration config = new MuleConfiguration();

        assertNotNull(descriptor.getInterceptors());
        assertEquals(0, descriptor.getInterceptors().size());

        assertEquals(config.getQueueProfile().getMaxOutstandingMessages(), descriptor.getQueueProfile()
            .getMaxOutstandingMessages());
        assertEquals(config.getPoolingProfile().getMaxIdle(), descriptor.getPoolingProfile().getMaxIdle());
        assertEquals(config.getPoolingProfile().getMaxWait(), descriptor.getPoolingProfile().getMaxWait());
        assertEquals(config.getPoolingProfile().getMaxActive(), descriptor.getPoolingProfile().getMaxActive());
        assertEquals("1.0", descriptor.getVersion());
        // assertEquals(2, descriptor.getInitialisationPolicy());

        assertNull(descriptor.getImplementation());
        assertNull(descriptor.getInboundEndpoint());
        assertNull(descriptor.getInboundTransformer());
        assertNull(descriptor.getName());
        assertNull(descriptor.getOutboundEndpoint());
        assertNull(descriptor.getOutboundTransformer());
        assertEquals(0, descriptor.getProperties().size());
    }

    public void testDescriptorNullValidation() throws Exception
    {
        UMODescriptor descriptor = new MuleDescriptor();

        try
        {
            descriptor.setExceptionListener(null);
            fail("setting exeption strategy to null should fail");
        }
        catch (RuntimeException e)
        {
            // expected
        }

        try
        {
            descriptor.setName(null);
            fail("setting name to null should fail");
        }
        catch (RuntimeException e)
        {
            // expected
        }

        try
        {
            descriptor.setImplementation(null);
            fail("setting implementation to null should fail");
        }
        catch (RuntimeException e)
        {
            // expected
        }

    }

    public void testImplementationValidation() throws Exception
    {
        UMODescriptor descriptor = new MuleDescriptor();

        try
        {
            descriptor.setImplementation(null);
            fail("setting implementation to null should fail");
        }
        catch (RuntimeException e)
        {
            // expected
        }

    }

    public void testEndpointValidation() throws Exception
    {
        UMODescriptor descriptor = getTestDescriptor("Terry", Orange.class.getName());
        TestExceptionStrategy es = new TestExceptionStrategy();
        descriptor.setExceptionListener(es);
        assertNotNull(descriptor.getOutboundEndpoint());
        assertNotNull(descriptor.getOutboundEndpoint().getConnector().getExceptionListener());

        // create receive endpoint
        UMOEndpoint endpoint = getTestEndpoint("test2", UMOEndpoint.ENDPOINT_TYPE_RECEIVER);
        descriptor.setInboundEndpoint(endpoint);
        // Add receive endpoint, this shoulbe set as default
        assertNotNull(descriptor.getInboundEndpoint().getConnector().getExceptionListener());
        // assertNotNull(descriptor.getReceiveEndpoint());
        // assertEquals(descriptor.getReceiveEndpoint(),
        // endpoint.getEndpointURI());
    }
}
