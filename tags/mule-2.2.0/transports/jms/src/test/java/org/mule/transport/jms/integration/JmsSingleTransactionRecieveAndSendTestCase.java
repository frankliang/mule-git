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

import javax.jms.Message;
import javax.jms.TextMessage;

public abstract class JmsSingleTransactionRecieveAndSendTestCase extends AbstractJmsFunctionalTestCase
{
    public JmsSingleTransactionRecieveAndSendTestCase(JmsVendorConfiguration config)
    {
        super(config);
    }

    protected String getConfigResources()
    {
        return "integration/jms-single-tx-receive-send-in-one-tx.xml";
    }

    public void testSingleTransactionBeginOrJoinAndAlwaysBegin() throws Exception
    {
        send(scenarioCommit);
        Message message = receive(scenarioReceive);
        assertNotNull(message);
        assertTrue(TextMessage.class.isAssignableFrom(message.getClass()));
        assertEquals(((TextMessage) message).getText(), AbstractJmsFunctionalTestCase.DEFAULT_OUTPUT_MESSAGE);
    }

}
