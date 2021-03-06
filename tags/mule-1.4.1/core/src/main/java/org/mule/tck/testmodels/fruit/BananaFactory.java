/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tck.testmodels.fruit;

import org.mule.util.ObjectFactory;

/**
 * <code>BananaFactory</code> is a test factory that creates Bananas
 */
public class BananaFactory implements ObjectFactory
{
    public Object create() throws Exception
    {
        return new Banana();
    }
}
