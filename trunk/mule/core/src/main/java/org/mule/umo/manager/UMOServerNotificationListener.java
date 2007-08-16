/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.umo.manager;

/**
 * <code>UMOServerNotificationListener</code> is an observer interface that ojects
 * can implement and register themselves with the Mule Server to receive
 * notifications when the server, model and components stop, start, initialise, etc.
 */
public interface UMOServerNotificationListener
{
    void onNotification(UMOServerNotification notification);
}
