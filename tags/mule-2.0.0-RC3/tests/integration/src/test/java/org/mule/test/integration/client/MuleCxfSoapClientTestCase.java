/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.client;

public class MuleCxfSoapClientTestCase extends MuleAxisSoapClientTestCase
{
    public String getSoapProvider()
    {
        return "cxf";
    }

    // TODO fix: xfire doesn't currently support overloaded methods
    public void testRequestResponseComplex2() throws Exception
    {
        // no op
    }
}
