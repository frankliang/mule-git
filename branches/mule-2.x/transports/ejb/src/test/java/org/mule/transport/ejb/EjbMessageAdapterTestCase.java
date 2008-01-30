/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ejb;

import org.mule.api.MessagingException;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.AbstractMessageAdapterTestCase;
import org.mule.transport.ejb.EjbMessageAdapter;

public class EjbMessageAdapterTestCase extends AbstractMessageAdapterTestCase
{

    public Object getValidMessage() throws Exception
    {
        return "Hello".getBytes();
    }

    public MessageAdapter createAdapter(Object payload) throws MessagingException
    {
        return new EjbMessageAdapter(payload);
    }

    public Object getInvalidMessage()
    {
        return null;
    }

}
