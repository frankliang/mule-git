/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.impl.internal.events;

import org.mule.umo.manager.UMOServerEvent;
import org.mule.umo.model.UMOModel;

/**
 * <code>ModelEvent</code> is fired when an event such as the model starting
 * occurs. The payload of this event will always be a reference to the model.
 * 
 * @see org.mule.umo.model.UMOModel
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class ModelEvent extends UMOServerEvent implements BlockingServerEvent
{
    public static final int MODEL_INITIALISING = MODEL_EVENT_ACTION_START_RANGE + 1;
    public static final int MODEL_INITIALISED = MODEL_EVENT_ACTION_START_RANGE + 2;
    public static final int MODEL_INITIALISING_LISTENERS = MODEL_EVENT_ACTION_START_RANGE + 3;
    public static final int MODEL_INITIALISED_LISTENERS = MODEL_EVENT_ACTION_START_RANGE + 4;
    public static final int MODEL_STARTING = MODEL_EVENT_ACTION_START_RANGE + 5;
    public static final int MODEL_STARTED = MODEL_EVENT_ACTION_START_RANGE + 6;
    public static final int MODEL_STOPPING = MODEL_EVENT_ACTION_START_RANGE + 7;
    public static final int MODEL_STOPPED = MODEL_EVENT_ACTION_START_RANGE + 8;
    public static final int MODEL_DISPOSING = MODEL_EVENT_ACTION_START_RANGE + 9;
    public static final int MODEL_DISPOSED = MODEL_EVENT_ACTION_START_RANGE + 10;

    private static final transient String[] ACTIONS = new String[] { "initialising", "initialised",
            "initialising listeners", "initialised listeners", "starting", "started", "stopping", "stopped",
            "disposing", "disposed" };

    public ModelEvent(UMOModel message, int action)
    {
        super(message, action);
        resourceIdentifier = message.getName();
    }

    protected String getPayloadToString()
    {
        return ((UMOModel) source).getName();
    }

    protected String getActionName(int action)
    {
        int i = action - MODEL_EVENT_ACTION_START_RANGE;
        if (i - 1 > ACTIONS.length) {
            return String.valueOf(action);
        }
        return ACTIONS[i - 1];
    }
}
