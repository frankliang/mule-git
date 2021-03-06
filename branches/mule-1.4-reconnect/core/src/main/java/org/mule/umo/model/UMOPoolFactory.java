/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.umo.model;

import org.mule.config.PoolingProfile;
import org.mule.umo.UMODescriptor;
import org.mule.util.ObjectFactory;
import org.mule.util.ObjectPool;

/**
 * <code>UMOPoolFactory</code> is a factory interface for created a component pool
 * instance
 */
public interface UMOPoolFactory
{
    ObjectPool createPool(UMODescriptor descriptor, UMOModel model, ObjectFactory factory, PoolingProfile pp);

    ObjectPool createPool(UMODescriptor descriptor, UMOModel model, PoolingProfile pp);
}
