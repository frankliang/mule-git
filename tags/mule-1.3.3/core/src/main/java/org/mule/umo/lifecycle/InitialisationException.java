/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.umo.lifecycle;

import org.mule.config.i18n.Message;

/**
 * <code>InitialisationException</code> is thrown by the initialise method defined
 * in the <code>org.mule.umo.lifecycle.Initialisable</code> interface. Init
 * initialisation exceptions are fatal and will cause the current Mule Instance to
 * shutdown
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class InitialisationException extends LifecycleException
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = -8402348927606781931L;

    /**
     * @param message the exception message
     * @param component the object that failed during a lifecycle method call
     */
    public InitialisationException(Message message, Object component)
    {
        super(message, component);
    }

    /**
     * @param message the exception message
     * @param cause the exception that cause this exception to be thrown
     * @param component the object that failed during a lifecycle method call
     */
    public InitialisationException(Message message, Throwable cause, Object component)
    {
        super(message, cause, component);
    }

    /**
     * @param cause the exception that cause this exception to be thrown
     * @param component the object that failed during a lifecycle method call
     */
    public InitialisationException(Throwable cause, Object component)
    {
        super(cause, component);
    }
}
