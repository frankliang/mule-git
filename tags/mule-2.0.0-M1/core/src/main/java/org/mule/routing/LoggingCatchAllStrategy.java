/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.routing;

import org.mule.impl.MuleEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOSession;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.routing.RoutingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>LoggingCatchAllStrategy</code> is a simple strategy that only logs any
 * events not caught by the router associated with this strategy. This should <b>not</b>
 * be used in production unless it is acceptible for events to be disposing.
 */

public class LoggingCatchAllStrategy extends AbstractCatchAllStrategy
{
    private static final Log logger = LogFactory.getLog(MuleEvent.class);

    public void setEndpoint(UMOEndpoint endpoint)
    {
        throw new UnsupportedOperationException("An endpoint cannot be set on this Catch All strategy");
    }

    public void setEndpoint(String endpoint)
    {
        throw new UnsupportedOperationException("An endpoint cannot be set on this Catch All strategy");
    }

    public UMOEndpoint getEndpoint()
    {
        return null;
    }

    public UMOMessage catchMessage(UMOMessage message, UMOSession session, boolean synchronous)
        throws RoutingException
    {
        logger.warn("Message: " + message + " was not dispatched on session: " + session
                    + ". No routing path was defined for it.");
        return null;
    }
}
