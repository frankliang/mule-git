/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.models;


import org.mule.model.direct.DirectModel;
import org.mule.tck.FunctionalTestCase;

public class ConfigureModelTestCase extends FunctionalTestCase
{

    protected String getConfigResources()
    {
        return "org/mule/test/integration/models/direct-pipeline-test-config.xml";
    }

    public void testConfigure()
    {
        assertTrue(muleContext.getRegistry().lookupModel("main") instanceof DirectModel);
        assertEquals("main", muleContext.getRegistry().lookupModel("main").getName());
    }
}
