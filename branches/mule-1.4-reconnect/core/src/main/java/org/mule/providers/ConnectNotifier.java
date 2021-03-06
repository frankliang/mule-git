/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers;

import org.mule.MuleManager;
import org.mule.config.ExceptionHelper;
import org.mule.impl.internal.notifications.ConnectionNotification;
import org.mule.umo.retry.UMORetryNotifier;
import org.mule.umo.retry.UMORetryContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO
 */
public class ConnectNotifier implements UMORetryNotifier
{
    /**
     * logger used by this class
     */
    protected transient final Log logger = LogFactory.getLog(ConnectNotifier.class);

    public void sucess(UMORetryContext context)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Successfully connected to " + context.getDescription());
        }

        fireConnectNotification(ConnectionNotification.CONNECTION_CONNECTED, context.getDescription());
    }

    public void failed(UMORetryContext context, Throwable e)
    {

        fireConnectNotification(ConnectionNotification.CONNECTION_FAILED, context.getDescription());

        if (logger.isErrorEnabled())
        {
            StringBuffer msg = new StringBuffer(512);
            msg.append("Failed to connect/reconnect: ").append(context.getDescription());
            Throwable t = ExceptionHelper.getRootException(e);
            msg.append(". Root Exception was: ").append(ExceptionHelper.writeException(t));
            logger.error(msg.toString());
        }
    }

    protected void fireConnectNotification(int action, String description)
    {
        MuleManager.getInstance().fireNotification(new ConnectionNotification(null, description, action));
    }

}
