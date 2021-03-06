/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformer.simple;

import org.mule.api.transformer.Transformer;
import org.mule.tck.AbstractTransformerTestCase;

public class ObjectByteArrayTransformersWithStringsTestCase extends AbstractTransformerTestCase
{
    private String testObject = "test";

    public Transformer getTransformer() throws Exception
    {
        return new ObjectToByteArray();
    }

    public Transformer getRoundTripTransformer() throws Exception
    {
        return new ByteArrayToObject();
    }

    public Object getTestData()
    {
        return testObject;
    }

    public Object getResultData()
    {
        return testObject.getBytes();
    }
}
