/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport;

import org.mule.tck.AbstractMuleTestCase;

public class KeyedPoolMessageDispatcherFactoryAdapterTestCase extends AbstractMuleTestCase
{

    public void testNullFactory()
    {
        try
        {
            new KeyedPoolMessageDispatcherFactoryAdapter(null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException iex)
        {
            // OK
        }
    }

}
