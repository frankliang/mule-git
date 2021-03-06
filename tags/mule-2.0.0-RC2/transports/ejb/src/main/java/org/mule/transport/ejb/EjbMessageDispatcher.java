/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.ejb;

import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.transport.rmi.RmiMessageDispatcher;

/**
 * Invokes a method on an EJB object stored in Jndi. A dispatcher is created for each
 * type of object invoked
 */
public class EjbMessageDispatcher extends RmiMessageDispatcher
{

    public EjbMessageDispatcher(ImmutableEndpoint endpoint)
    {
        super(endpoint);
    }
}
