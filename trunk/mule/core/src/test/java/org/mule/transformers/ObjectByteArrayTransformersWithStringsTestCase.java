/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformers;

import org.mule.tck.AbstractTransformerTestCase;
import org.mule.transformers.simple.ByteArrayToObject;
import org.mule.transformers.simple.ObjectToByteArray;
import org.mule.umo.transformer.UMOTransformer;

public class ObjectByteArrayTransformersWithStringsTestCase extends AbstractTransformerTestCase
{
    private String testObject = "test";

    public UMOTransformer getTransformer() throws Exception
    {
        return new ObjectToByteArray();
    }

    public UMOTransformer getRoundTripTransformer() throws Exception
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
