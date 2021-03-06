/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jms.test;

import org.mule.api.MuleContext;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.transport.jms.JmsConnector;

public class TestJmsConnector extends JmsConnector
{
    public TestJmsConnector(MuleContext context) throws InitialisationException
    {
        super(context);
        setConnectionFactory(new TestConnectionFactory());
    }
}
