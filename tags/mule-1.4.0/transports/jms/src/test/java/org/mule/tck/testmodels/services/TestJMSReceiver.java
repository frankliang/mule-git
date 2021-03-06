/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tck.testmodels.services;

import org.mule.impl.RequestContext;
import org.mule.util.StringMessageUtils;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestJMSReceiver extends TestReceiver
{
    public String receive(TextMessage message) throws Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Received: " + message.getText() + " Number: " + inc() + " in thread: "
                         + Thread.currentThread().getName());
            logger.debug("Message ID is: " + message.getJMSMessageID());
        }

        return "Received: " + message.getText();
    }
}
