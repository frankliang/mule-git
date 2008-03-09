/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.util.expression;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.tck.AbstractMuleTestCase;

import java.sql.Timestamp;

public class ExpressionEvaluatorManagerTestCase extends AbstractMuleTestCase
{
    public void testManager() throws Exception
    {
        MuleMessage message = new DefaultMuleMessage("test");
        Object o = ExpressionEvaluatorManager.evaluate("function:uuid", message);
        assertNotNull(o);
        o = ExpressionEvaluatorManager.evaluate("function:now", message);
        assertNotNull(o);
        assertTrue(o instanceof Timestamp);
    }

    public void testRegistration() throws Exception
    {
        try
        {
            ExpressionEvaluatorManager.registerEvaluator(new MapPayloadExpressionEvaluator());
            fail("extractor already exists");
        }
        catch (IllegalArgumentException e)
        {
            //Expected
        }

        try
        {
            ExpressionEvaluatorManager.registerEvaluator(null);
            fail("null extractor");
        }
        catch (IllegalArgumentException e)
        {
            //Expected
        }
        assertNull(ExpressionEvaluatorManager.unregisterEvaluator(null));

    }
}
