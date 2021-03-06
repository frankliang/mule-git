/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.util.properties;

import org.mule.api.MuleMessage;
import org.mule.transport.NullPayload;

/**
 * Looks up the property on the message using the name given.
 */
public class MessageHeaderPropertyExtractor implements PropertyExtractor
{
    public static final String NAME = "header";

    public Object getProperty(String name, Object message)
    {
        if (message instanceof MuleMessage)
        {
            if (name.equalsIgnoreCase("payload"))
            {
                Object payload = ((MuleMessage) message).getPayload();
                return (payload instanceof NullPayload ? null : payload);
            }
            else
            {
                return ((MuleMessage) message).getProperty(name);
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return NAME;
    }

    /** {@inheritDoc} */
    public void setName(String name)
    {
        throw new UnsupportedOperationException("setName");
    }
}
