/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.model;

import org.mule.management.stats.ComponentStatistics;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.Disposable;
import org.mule.umo.lifecycle.Startable;
import org.mule.umo.lifecycle.Stoppable;
import org.mule.util.queue.QueueSession;

import javax.resource.spi.work.Work;

/**
 * <code>MuleProxy</code> is a proxy to a UMO. It is an object that 
 * can be executed in it's own thread.
 */

public interface MuleProxy extends Work, Startable, Stoppable, Disposable
{

    /**
     * Sets the current event being processed
     * 
     * @param event the event being processed
     */
    void onEvent(QueueSession session, UMOEvent event);

    ComponentStatistics getStatistics();

    void setStatistics(ComponentStatistics stat);

    /**
     * Makes a synchronous call on the UMO
     * 
     * @param event the event to pass to the UMO
     * @return the return event from the UMO
     * @throws UMOException if the call fails
     */
    Object onCall(UMOEvent event) throws UMOException;

    /**
     * When an exception occurs this method can be called to invoke the configured
     * UMOExceptionStrategy on the UMO
     * 
     * @param exception If the UMOExceptionStrategy implementation fails
     */
    void handleException(Exception exception);

    /**
     * Determines if the proxy is suspended
     * 
     * @return true if the proxy (and the UMO) are suspended
     */
    boolean isSuspended();

    /**
     * Controls the suspension of the UMO event processing
     */
    void suspend();

    /**
     * Triggers the UMO to resume processing of events if it is suspended
     */
    void resume();
}
