/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.soap.xfire;

import org.mule.api.transport.DispatchException;
import org.mule.extras.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.soap.xfire.testmodels.CustomFault;
import org.mule.transport.soap.xfire.testmodels.XFireEnabledFaultMessage;

import org.codehaus.xfire.fault.XFireFault;


public class XFireComponentExceptionStrategyTestCase extends FunctionalTestCase
{
    public void testDefaultComponentExceptionStrategy() throws Exception
    {
        MuleClient client = new MuleClient();

        try
        {
            client.send("xfire:http://localhost:63181/services/XFireDefault?method=testXFireException", "TEST", null);
        }
        catch (DispatchException ex)
        {
            final Throwable t = ex.getCause();
            assertNotNull("Cause should've been filled in.", t);
            assertTrue(t instanceof XFireFault);
        }
    }

    public void testHandledException() throws Exception
    {
        MuleClient client = new MuleClient();

        try
        {
            client.send("xfire:http://localhost:63181/services/XFireWithExceptionStrategy?method=testXFireException", "TEST", null);
        }
        catch (DispatchException ex)
        {
            final Throwable t = ex.getCause();
            assertNotNull("Cause should've been filled in.", t);
            assertTrue(t instanceof XFireEnabledFaultMessage);
            XFireEnabledFaultMessage xfireMsg = (XFireEnabledFaultMessage) t;
            CustomFault fault = xfireMsg.getFaultInfo();
            assertNotNull(fault);
            assertEquals("Custom Exception Message", fault.getDescription());
        }
    }


    public void testUnhandledException() throws Exception
    {
        MuleClient client = new MuleClient();

        try
        {
            client.send("xfire:http://localhost:63181/services/XFireWithExceptionStrategy?method=testNonXFireException", "TEST", null);
        }
        catch (DispatchException ex)
        {
            final Throwable t = ex.getCause();
            assertNotNull("Cause should've been filled in.", t);
            assertTrue(t instanceof XFireFault);
        }
    }

    protected String getConfigResources()
    {
        return "xfire-exception-strategy-conf.xml";
    }
}
