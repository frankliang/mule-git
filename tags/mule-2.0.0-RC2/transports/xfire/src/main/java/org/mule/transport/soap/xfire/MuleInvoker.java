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

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleProperties;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.NullPayload;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.invoker.Invoker;

/**
 * Invokes a Mule Service via an XFire binding.
 */
public class MuleInvoker implements Invoker
{
    private final AbstractMessageReceiver receiver;
    private final boolean synchronous;

    public MuleInvoker(AbstractMessageReceiver receiver, boolean synchronous)
    {
        this.receiver = receiver;
        this.synchronous = synchronous;
    }

    public Object invoke(Method method, Object[] objects, MessageContext messageContext) throws XFireFault
    {
        MuleMessage message = null;
        try
        {
            XFireMessageAdapter messageAdapter = (XFireMessageAdapter)receiver.getConnector()
                .getMessageAdapter(objects);
            messageAdapter.setMessageContext(messageContext);
            messageAdapter.setProperty(MuleProperties.MULE_METHOD_PROPERTY, method);

            message = receiver.routeMessage(new DefaultMuleMessage(messageAdapter), synchronous);
        }
        catch (MuleException e)
        {
            throw new XFireFault(e);
        }

        if (message != null)
        {
            if (message.getExceptionPayload() != null)
            {
                QName code = new QName(String.valueOf(message.getExceptionPayload().getCode()));
                throw new XFireFault(message.getExceptionPayload().getMessage(),
                    message.getExceptionPayload().getException(), code);
            }
            else if (message.getPayload() instanceof NullPayload)
            {
                return null;
            }
            else
            {
                return message.getPayload();
            }
        }
        else
        {
            return null;
        }
    }

    public ImmutableEndpoint getEndpoint()
    {
        return receiver.getEndpoint();
    }
}
