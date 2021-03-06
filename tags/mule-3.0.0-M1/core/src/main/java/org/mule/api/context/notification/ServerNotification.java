/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.context.notification;

import org.mule.MuleServer;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.util.ClassUtils;

import java.util.EventObject;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * <code>ServerNotification</code> is an event triggered by something happening
 * in the Server itself such as the server starting or a service being registered.
 */
public abstract class ServerNotification extends EventObject
{

    public static final int NO_ACTION_ID = Integer.MIN_VALUE;
    public static final String NO_ACTION_NAME = "none";

    public static final String TYPE_TRACE = "trace";
    public static final String TYPE_INFO = "info";
    public static final String TYPE_WARNING = "warn";
    public static final String TYPE_ERROR = "error";
    public static final String TYPE_FATAL = "fatal";

    protected static final int CONTEXT_EVENT_ACTION_START_RANGE = 100;
    protected static final int MODEL_EVENT_ACTION_START_RANGE = 200;
    protected static final int SERVICE_EVENT_ACTION_START_RANGE = 300;
    protected static final int SECURITY_EVENT_ACTION_START_RANGE = 400;
    protected static final int MANAGEMENT_EVENT_ACTION_START_RANGE = 500;
    protected static final int ADMIN_EVENT_ACTION_START_RANGE = 600;
    protected static final int CONNECTION_EVENT_ACTION_START_RANGE = 700;
    protected static final int MESSAGE_EVENT_ACTION_START_RANGE = 800;
    protected static final int SPACE_EVENT_ACTION_START_RANGE = 900;
    protected static final int REGISTRY_EVENT_ACTION_START_RANGE = 1000;
    protected static final int EXCEPTION_EVENT_ACTION_START_RANGE = 1100;
    protected static final int TRANSACTION_EVENT_ACTION_START_RANGE = 1200;
    protected static final int ROUTING_EVENT_ACTION_START_RANGE = 1300;
    protected static final int COMPONENT_EVENT_ACTION_START_RANGE = 1400;
    protected static final int CUSTOM_EVENT_ACTION_START_RANGE = 100000;

    public static final int NULL_ACTION = 0;
    public static final Object NULL_MESSAGE = "";

    public final String EVENT_NAME = ClassUtils.getClassName(getClass());

    protected String serverId;

    protected long timestamp;

    protected int action = NULL_ACTION;
    private static Map actionIdToName = new ConcurrentHashMap();
    private static Map actionNameToId = new ConcurrentHashMap();

    /**
     * The resourceIdentifier is used when firing inbound server notifications such
     * as Admin notifications or other action notifications triggered by an external
     * source Used to associate the event with a particular resource. For example, if
     * the event was a ServiceNotification the resourceIdentifier could be the name
     * of a particular service
     */
    protected String resourceIdentifier = null;

    public ServerNotification(Object message, int action)
    {
        this(message, action, null);
    }

    public ServerNotification(Object message, int action, String resourceIdentifier)
    {
        super((message == null ? NULL_MESSAGE : message));
        this.action = action;
        this.resourceIdentifier = resourceIdentifier;

        MuleContext mc = (message instanceof MuleContext ? (MuleContext)message : MuleServer.getMuleContext());

        serverId = generateId(mc);

        timestamp = System.currentTimeMillis();
    }

    protected static String generateId(MuleContext context)
    {
        return context.getConfiguration().getDomainId() + "." + context.getConfiguration().getClusterId() + "." + context.getConfiguration().getId();
    }

    protected static MuleMessage cloneMessage(MuleMessage message)
    {
        if(message==null) {
            return null;
        }
        synchronized (message)
        {
            return new DefaultMuleMessage(message.getPayload(), message);
        }
    }

    public int getAction()
    {
        return action;
    }

    public String getServerId()
    {
        return serverId;
    }

    public String getResourceIdentifier()
    {
        return resourceIdentifier;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public boolean isResourceIdentifierAnUri()
    {
        return MuleEndpointURI.isMuleUri(resourceIdentifier);
    }

    public String toString()
    {
        return EVENT_NAME + "{" + "action=" + getActionName(action) + ", resourceId=" + resourceIdentifier
                + ", serverId=" + serverId + ", timestamp=" + timestamp + "}";
    }

    protected String getPayloadToString()
    {
        return source.toString();
    }

    public String getType()
    {
        return TYPE_INFO;
    }

    public String getActionName()
    {
        return getActionName(action);
    }


    protected static synchronized void registerAction(String name, int i)
    {
        String lowerCaseName = name.toLowerCase();
        Integer id = new Integer(i);
        if (actionNameToId.containsKey(lowerCaseName))
        {
            throw new IllegalStateException("Action " + name + " already registered");
        }
        if (actionIdToName.containsKey(id))
        {
            throw new IllegalStateException("Action id " + i + " already registered");
        }
        actionIdToName.put(id, lowerCaseName);
        actionNameToId.put(lowerCaseName, id);
    }

    public static String getActionName(int action)
    {
        if (action == NO_ACTION_ID)
        {
            return NO_ACTION_NAME;
        }
        Integer key = new Integer(action);
        if (actionIdToName.containsKey(key))
        {
            return (String) actionIdToName.get(key);
        }
        else
        {
            throw new IllegalArgumentException("No action with id: " + action);
        }
    }

    public static int getActionId(String action)
    {
        String lowerCaseName = action.toLowerCase();
        if (actionNameToId.containsKey(lowerCaseName))
        {
            return ((Integer) actionNameToId.get(lowerCaseName)).intValue();
        }
        else
        {
            throw new IllegalArgumentException("No action called: " + action);
        }
    }

}
