/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.persistence;

import org.mule.api.context.notification.ServerNotification;

/**
 * TODO
 */
public class PersistenceNotification extends ServerNotification 
{
    public static final int PERSISTABLE_NOT_READY = 0;
    public static final int PERSISTABLE_READY = 1;

    static {
        registerAction("ready", PERSISTABLE_READY);
        registerAction("not ready", PERSISTABLE_NOT_READY);
    }
    
    public PersistenceNotification(Persistable object, int action)
    {
        super(object, action);
    }

    protected boolean isReady()
    {
        return (action == PERSISTABLE_READY ? true : false);
    }


}
