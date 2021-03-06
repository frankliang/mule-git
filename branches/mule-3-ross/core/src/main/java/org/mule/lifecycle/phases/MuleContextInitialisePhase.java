/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.lifecycle.phases;

import org.mule.api.agent.Agent;
import org.mule.api.component.Component;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.model.Model;
import org.mule.api.service.Service;
import org.mule.api.transport.Connector;
import org.mule.lifecycle.DefaultLifecyclePhase;
import org.mule.lifecycle.LifecycleObject;
import org.mule.lifecycle.NotificationLifecycleObject;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Since all objects are created and initialised in the registry, the Initialise lifecyclePhase is always
 * taken care of by the Registry, hence this class extends {@link org.mule.lifecycle.ContainerManagedLifecyclePhase}
 */
public class MuleContextInitialisePhase extends DefaultLifecyclePhase
{
    public MuleContextInitialisePhase()
    {
        super(Initialisable.PHASE_NAME, Initialisable.class, Disposable.PHASE_NAME);
        registerSupportedPhase(NotInLifecyclePhase.PHASE_NAME);

        Set<LifecycleObject> startOrderedObjects = new LinkedHashSet<LifecycleObject>();
        startOrderedObjects.add(new NotificationLifecycleObject(Connector.class));
        startOrderedObjects.add(new NotificationLifecycleObject(Agent.class));
        startOrderedObjects.add(new NotificationLifecycleObject(Model.class));
        startOrderedObjects.add(new NotificationLifecycleObject(Service.class));
        startOrderedObjects.add(new NotificationLifecycleObject(Initialisable.class));
        setOrderedLifecycleObjects(startOrderedObjects);
        setIgnoredObjectTypes(new Class[]{Component.class});
    }
}