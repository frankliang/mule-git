/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the BSD style
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.tcp.integration;

import org.mule.providers.tcp.protocols.DefaultProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.SerializationUtils;

public class CustomSerializationProtocol extends DefaultProtocol
{

    // @Override
    public void write(OutputStream os, Object data) throws IOException
    {
        if (data instanceof NonSerializableMessageObject)
        {
            NonSerializableMessageObject in = (NonSerializableMessageObject)data;

            // do serialization... will use normal Serialization to simplify code...
            MessageObject serializableObject = new MessageObject(in.i, in.s, in.b);

            write(os, SerializationUtils.serialize(serializableObject));
        }
        // TODO SF: isn't the "else" branch missing here? :)
    }

    // @Override
    public Object read(InputStream is) throws IOException
    {
        byte[] tmp = (byte[])super.read(is);

        if (tmp == null)
        {
            return null;
        }
        else
        {
            MessageObject serializableObject = (MessageObject)SerializationUtils.deserialize(tmp);
            return new NonSerializableMessageObject(serializableObject.i, serializableObject.s,
                serializableObject.b);
        }
    }

}
