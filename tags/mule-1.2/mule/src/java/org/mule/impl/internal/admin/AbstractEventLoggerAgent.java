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
package org.mule.impl.internal.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.impl.internal.events.AdminEventListener;
import org.mule.impl.internal.events.ComponentEventListener;
import org.mule.impl.internal.events.ConnectionEventListener;
import org.mule.impl.internal.events.CustomEventListener;
import org.mule.impl.internal.events.ManagementEventListener;
import org.mule.impl.internal.events.ManagerEventListener;
import org.mule.impl.internal.events.MessageEventListener;
import org.mule.impl.internal.events.ModelEventListener;
import org.mule.impl.internal.events.SecurityEventListener;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.manager.UMOAgent;
import org.mule.umo.manager.UMOManager;
import org.mule.umo.manager.UMOServerEvent;
import org.mule.umo.manager.UMOServerEventListener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <code>AbstractEventLoggerAgent</code> Receives Mule server events and logs
 * them and can optionally route them to an endpoint
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public abstract class AbstractEventLoggerAgent implements UMOAgent
{
    /**
     * The logger used for this class
     */
    protected transient Log logger = LogFactory.getLog(getClass());


    private String name;

    private boolean ignoreManagerEvents = false;
    private boolean ignoreModelEvents = false;
    private boolean ignoreComponentEvents = false;
    private boolean ignoreConnectionEvents = false;
    private boolean ignoreSecurityEvents = false;
    private boolean ignoreManagementEvents = false;
    private boolean ignoreCustomEvents = false;
    private boolean ignoreAdminEvents = false;
    private boolean ignoreMessageEvents = false;

    private Set listeners = new HashSet();

    /**
     * Gets the name of this agent
     * 
     * @return the agent name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of this agent
     * 
     * @param name the name of the agent
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public void start() throws UMOException
    {
    }

    public void stop() throws UMOException
    {
    }

    public void dispose()
    {
    }

    public void registered()
    {
    }

    public void unregistered()
    {
        for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            UMOServerEventListener listener = (UMOServerEventListener) iterator.next();
            MuleManager.getInstance().unregisterListener(listener);
        }
    }

    public boolean isIgnoreManagerEvents()
    {
        return ignoreManagerEvents;
    }

    public void setIgnoreManagerEvents(boolean ignoreManagerEvents)
    {
        this.ignoreManagerEvents = ignoreManagerEvents;
    }

    public boolean isIgnoreModelEvents()
    {
        return ignoreModelEvents;
    }

    public void setIgnoreModelEvents(boolean ignoreModelEvents)
    {
        this.ignoreModelEvents = ignoreModelEvents;
    }

    public boolean isIgnoreComponentEvents()
    {
        return ignoreComponentEvents;
    }

    public void setIgnoreComponentEvents(boolean ignoreComponentEvents)
    {
        this.ignoreComponentEvents = ignoreComponentEvents;
    }

    public boolean isIgnoreSecurityEvents()
    {
        return ignoreSecurityEvents;
    }

    public void setIgnoreSecurityEvents(boolean ignoreSecurityEvents)
    {
        this.ignoreSecurityEvents = ignoreSecurityEvents;
    }

    public boolean isIgnoreManagementEvents()
    {
        return ignoreManagementEvents;
    }

    public void setIgnoreManagementEvents(boolean ignoreManagementEvents)
    {
        this.ignoreManagementEvents = ignoreManagementEvents;
    }

    public boolean isIgnoreCustomEvents()
    {
        return ignoreCustomEvents;
    }

    public void setIgnoreCustomEvents(boolean ignoreCustomEvents)
    {
        this.ignoreCustomEvents = ignoreCustomEvents;
    }

    public boolean isIgnoreAdminEvents()
    {
        return ignoreAdminEvents;
    }

    public void setIgnoreAdminEvents(boolean ignoreAdminEvents)
    {
        this.ignoreAdminEvents = ignoreAdminEvents;
    }

    public boolean isIgnoreConnectionEvents()
    {
        return ignoreConnectionEvents;
    }

    public void setIgnoreConnectionEvents(boolean ignoreConnectionEvents)
    {
        this.ignoreConnectionEvents = ignoreConnectionEvents;
    }

    public final void initialise() throws InitialisationException
    {
        doInitialise();
        UMOManager manager = MuleManager.getInstance();
        if (!ignoreManagerEvents) {
            UMOServerEventListener l = new ManagerEventListener() {
                public void onEvent(UMOServerEvent event)
                {
                    logEvent(event);
                }
            };
            manager.registerListener(l);
            listeners.add(l);
        }
        if (!ignoreModelEvents) {
            UMOServerEventListener l = new ModelEventListener() {
                public void onEvent(UMOServerEvent event)
                {
                    logEvent(event);
                }
            };
            manager.registerListener(l);
            listeners.add(l);
        }
        if (!ignoreComponentEvents) {
            UMOServerEventListener l = new ComponentEventListener() {
                public void onEvent(UMOServerEvent event)
                {
                    logEvent(event);
                }
            };
            manager.registerListener(l);
            listeners.add(l);
        }
        if (!ignoreSecurityEvents) {
            UMOServerEventListener l = new SecurityEventListener() {
                public void onEvent(UMOServerEvent event)
                {
                    logEvent(event);
                }
            };
            manager.registerListener(l);
            listeners.add(l);
        }

        if (!ignoreManagementEvents) {
            UMOServerEventListener l = new ManagementEventListener() {
                public void onEvent(UMOServerEvent event)
                {
                    logEvent(event);
                }
            };
            manager.registerListener(l);
            listeners.add(l);
        }

        if (!ignoreCustomEvents) {
            UMOServerEventListener l = new CustomEventListener() {
                public void onEvent(UMOServerEvent event)
                {
                    logEvent(event);
                }
            };
            manager.registerListener(l);
            listeners.add(l);
        }

        if (!ignoreConnectionEvents) {
            UMOServerEventListener l = new ConnectionEventListener() {
                public void onEvent(UMOServerEvent event)
                {
                    logEvent(event);
                }
            };
            manager.registerListener(l);
            listeners.add(l);
        }

        if (!ignoreAdminEvents) {
            UMOServerEventListener l = new AdminEventListener() {
                public void onEvent(UMOServerEvent event)
                {
                    logEvent(event);
                }
            };
            manager.registerListener(l);
            listeners.add(l);
        }

        if(!ignoreMessageEvents && !MuleManager.getConfiguration().isEnableMessageEvents()) {
            logger.warn("EventLogger agent has been asked to log message events, but the MuleManager is configured not to fire Message events");
        } else if (!ignoreMessageEvents) {
            UMOServerEventListener l = new MessageEventListener() {
                public void onEvent(UMOServerEvent event)
                {
                    logEvent(event);
                }
            };
            manager.registerListener(l);
            listeners.add(l);
        }

    }

    protected abstract void doInitialise() throws InitialisationException;

    protected abstract void logEvent(UMOServerEvent e);
}
