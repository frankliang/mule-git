/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.stdio;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.DispatchException;
import org.mule.transport.AbstractMessageDispatcher;
import org.mule.transport.stdio.i18n.StdioMessages;
import org.mule.util.StringUtils;

import java.io.OutputStream;

/**
 * <code>StdioMessageDispatcher</code> is a simple stream dispatcher that obtains
 * a stream from the Stream Connector to write to. This is only really useful for
 * testing purposes right now when writing to System.in and System.out. However, it
 * is feasible to set any OutputStream on the Stream connector and have that written
 * to.
 */

public class StdioMessageDispatcher extends AbstractMessageDispatcher
{
    private final StdioConnector connector;

    public StdioMessageDispatcher(OutboundEndpoint endpoint)
    {
        super(endpoint);
        this.connector = (StdioConnector)endpoint.getConnector();

        // apply connector-specific properties
        if (connector instanceof PromptStdioConnector)
        {
            PromptStdioConnector ssc = (PromptStdioConnector)connector;

            String outputMessage = (String)endpoint.getProperties().get("outputMessage");
            if (outputMessage != null)
            {
                ssc.setOutputMessage(outputMessage);
            }
        }
    }

    protected synchronized void doDispatch(MuleEvent event) throws Exception
    {
        OutputStream out = connector.getOutputStream();

        if (out == null)
        {
            throw new DispatchException(
                StdioMessages.couldNotFindStreamWithName(event.getEndpoint().getEndpointURI().getAddress()), 
                event.getMessage(), event.getEndpoint());
        }

        // TODO - remove this ugliness
        if (connector instanceof PromptStdioConnector)
        {
            PromptStdioConnector ssc = (PromptStdioConnector)connector;
            if (StringUtils.isNotBlank(ssc.getOutputMessage()))
            {
                out.write(ssc.getOutputMessage().getBytes());
            }
        }

        Object data = event.transformMessage();
        if (data instanceof byte[])
        {
            out.write((byte[])data);
        }
        else
        {
            out.write(data.toString().getBytes());
        }

        out.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.api.transport.Connector#send(org.mule.api.MuleEvent)
     */
    protected MuleMessage doSend(MuleEvent event) throws Exception
    {
        doDispatch(event);
        return event.getMessage();
    }

    protected void doDispose()
    {
        // template method
    }

    protected void doConnect() throws Exception
    {
        // template method
    }

    protected void doDisconnect() throws Exception
    {
        // template method
    }



}
