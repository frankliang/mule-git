/*
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved. http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 *
 */

package org.mule.providers.stream;

import com.mockobjects.dynamic.Mock;

import org.mule.impl.DefaultExceptionStrategy;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.tck.providers.AbstractMessageReceiverTestCase;
import org.mule.tck.testmodels.fruit.Orange;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageReceiver;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class StreamMessageReceiverTestCase extends AbstractMessageReceiverTestCase
{

    public void testReceiver() throws Exception
    {
        // FIX A bit hard testing receive from a unit test as we need to reg
        // listener etc
        // file endpoint functiona tests for this
    }

    public void testOtherProperties() throws Exception
    {
        StreamMessageReceiver receiver = (StreamMessageReceiver) getMessageReceiver();

        MuleDescriptor descriptor = getTestDescriptor("orange", Orange.class.getName());
        assertNotNull(descriptor);

        UMOComponent component = getTestComponent(descriptor);
        assertNotNull(component);

        endpoint.getConnector().startConnector();
        Mock connector = new Mock(UMOConnector.class);
        connector.expectAndReturn("getExceptionListener", new DefaultExceptionStrategy());

        receiver.setFrequency(1001);
        receiver.setInputStream(System.in);

        assertTrue(receiver.getFrequency() == 1001);
        receiver.setFrequency(0);
        assertTrue(receiver.getFrequency() == StreamMessageReceiver.DEFAULT_POLL_FREQUENCY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.tck.providers.AbstractMessageReceiverTestCase#getMessageReceiver()
     */
    public UMOMessageReceiver getMessageReceiver() throws InitialisationException
    {
        return new StreamMessageReceiver(endpoint.getConnector(), component, endpoint, new Long(1000));
    }

    public UMOEndpoint getEndpoint() throws Exception
    {
        endpoint = new MuleEndpoint("stream://System", true);
        return endpoint;
    }
}
