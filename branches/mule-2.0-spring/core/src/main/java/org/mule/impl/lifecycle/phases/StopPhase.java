/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.impl.lifecycle.phases;

import org.mule.impl.internal.notifications.ManagerNotification;
import org.mule.impl.lifecycle.LifecyclePhase;
import org.mule.impl.lifecycle.NotificationLifecycleObject;
import org.mule.registry.Registry;
import org.mule.umo.UMOManagementContext;
import org.mule.umo.lifecycle.Initialisable;
import org.mule.umo.lifecycle.Startable;
import org.mule.umo.lifecycle.Stoppable;
import org.mule.umo.manager.UMOAgent;
import org.mule.umo.model.UMOModel;
import org.mule.umo.provider.UMOConnector;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * TODO
 */
public class StopPhase extends LifecyclePhase
{
    public StopPhase()
    {
        this(new Class[]{Registry.class, UMOManagementContext.class});
    }

    public StopPhase(Class[] ignorredObjects)
    {
        super(Stoppable.PHASE_NAME, Stoppable.class);

        Set stopOrderedObjects = new LinkedHashSet();
        stopOrderedObjects.add(new NotificationLifecycleObject(UMOConnector.class));
        stopOrderedObjects.add(new NotificationLifecycleObject(UMOAgent.class));
        stopOrderedObjects.add(new NotificationLifecycleObject(UMOModel.class, ManagerNotification.class,
                ManagerNotification.getActionName(ManagerNotification.MANAGER_STOPPING_MODELS),
                ManagerNotification.getActionName(ManagerNotification.MANAGER_STOPPED_MODELS)));
        stopOrderedObjects.add(new NotificationLifecycleObject(Stoppable.class));

        setIgnorredObjectTypes(ignorredObjects);
        setOrderedLifecycleObjects(stopOrderedObjects);
        registerSupportedPhase(Startable.PHASE_NAME);
        registerSupportedPhase(Initialisable.PHASE_NAME);
    }
}