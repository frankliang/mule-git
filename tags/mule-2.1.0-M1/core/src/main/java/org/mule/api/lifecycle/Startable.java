/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.lifecycle;

import org.mule.api.MuleException;

/**
 * <code>Startable</code> provides an object with a {@link #start()} method
 * which gets called when the Mule instance gets started.  This is mostly used by
 * infrastructure components, but can also be implemented by service objects
 *
 * @see LifecycleAdapter
 */
public interface Startable
{

    static final String PHASE_NAME = "start";

    LifecycleTransitionResult start() throws MuleException;

}
