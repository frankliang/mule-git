/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jms.integration;

import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;

/**
 * TODO this test does not use the Test scenarios, I think it would need a new Method sendAndReceive
 * It might make sense to leave this test as is because it tests that the client also works witrh ReplyTo correctly
 */
public abstract class JmsTemporaryReplyToTestCase extends AbstractJmsFunctionalTestCase
{
    public JmsTemporaryReplyToTestCase(JmsVendorConfiguration config)
    {
        super(config);
    }

    protected String getConfigResources()
    {
        return "integration/jms-temporary-replyTo.xml";
    }

    public void testTemporaryReplyEnabled() throws MuleException
    {
        MuleClient muleClient = new MuleClient();
        MuleMessage response = muleClient.send("vm://in1", TEST_MESSAGE, null);
        assertEquals(TEST_MESSAGE + " TestService1", response.getPayload());
    }

    public void testTemporaryReplyDisabled() throws MuleException
    {
        MuleClient muleClient = new MuleClient();
        MuleMessage response = muleClient.send("vm://in2", TEST_MESSAGE, null);
        //We get the original message back, not the result from the remote component
        assertEquals(TEST_MESSAGE, response.getPayload());
    }

    public void testDisableTempraryReplyOnTheConeector() throws MuleException
    {
        MuleClient muleClient = new MuleClient();
        MuleMessage response = muleClient.send("vm://in3", TEST_MESSAGE, null);
        //We get the original message back, not the result from the remote component
        assertEquals(TEST_MESSAGE, response.getPayload());

    }

    public void testExplicitReplyToAsyncSet() throws MuleException
    {
        MuleClient muleClient = new MuleClient();
        MuleMessage response = muleClient.send("vm://in4", TEST_MESSAGE, null);
        //We get the original message back, not the result from the remote component
        assertEquals(TEST_MESSAGE + " TestService1", response.getPayload());

    }

}