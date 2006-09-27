/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl;

import org.apache.commons.lang.ObjectUtils;
import org.mule.impl.message.ExceptionPayload;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;

/**
 * <code>DefaultExceptionStrategy</code> Provides a default exception handling
 * strategy. The class final thus to change exception handling behaviour the
 * user must reimplemented the ExceptionListener Interface
 * 
 * @author Ross Mason
 * @version $Revision$
 */

public class DefaultExceptionStrategy extends AbstractExceptionListener
{
    public void handleMessagingException(UMOMessage message, Throwable t)
    {
        defaultHandler(t);
        routeException(message, null, t);
    }

    public void handleRoutingException(UMOMessage message, UMOImmutableEndpoint endpoint, Throwable t)
    {
        defaultHandler(t);
        routeException(message, endpoint, t);
    }

    public void handleLifecycleException(Object component, Throwable t)
    {
        defaultHandler(t);
        logger.error("The object that failed was: \n" + ObjectUtils.toString(component, "null"));
        markTransactionForRollback();
    }

    public void handleStandardException(Throwable t)
    {
        defaultHandler(t);
        markTransactionForRollback();
    }

    protected void defaultHandler(Throwable t)
    {
        logException(t);
        if (RequestContext.getEvent() != null) {
            RequestContext.setExceptionPayload(new ExceptionPayload(t));
        }
    }
}
