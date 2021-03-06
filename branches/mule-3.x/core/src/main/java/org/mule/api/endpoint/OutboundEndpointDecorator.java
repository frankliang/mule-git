/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.api.endpoint;

import org.mule.api.MuleException;
import org.mule.api.MuleMessage;

/**
 * Used on custom endpoints that provide additional processing to service or message to handle the way certain
 * types of messages are handled.  For example, the ATOM endpoint adds an inbound router and filter for splitting
 * atom feeds.
 */
public interface OutboundEndpointDecorator
{
    /**
     * This message will be invoked whenever a message passes throw the endpoint.  Implementors can modify the
     * message on the way through. The message returned from this method will continue to be processed. If null is returned
     * the message flow is halted
     *
     * @param message the current message being processed
     * @return true if message processing should continue, false otherwise
     *         the message flow is halted
     * @throws MuleException if the message processing causes an exception
     */
    boolean onMessage(MuleMessage message) throws MuleException;

}
