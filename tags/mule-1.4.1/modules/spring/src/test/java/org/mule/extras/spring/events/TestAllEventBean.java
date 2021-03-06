/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extras.spring.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.tck.functional.EventCallback;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * <code>TestMuleEventBean</code> is a MuleEventBean for testing with the
 * MuleEventMulticaster.
 */

public class TestAllEventBean implements MuleEventListener, ApplicationListener
{
    private final Log logger = LogFactory.getLog(this.getClass());
    private EventCallback eventCallback;

    public void onApplicationEvent(ApplicationEvent event)
    {
        logger.debug("Received message: " + event);

        if (eventCallback != null)
        {
            try
            {
                eventCallback.eventReceived(null, event);
            }
            catch (Exception e1)
            {
                throw new RuntimeException("Callback failed: " + e1.getMessage(), e1);
            }
        }
    }

    public EventCallback getEventCallback()
    {
        return eventCallback;
    }

    public void setEventCallback(EventCallback eventCallback)
    {
        this.eventCallback = eventCallback;
    }

}
