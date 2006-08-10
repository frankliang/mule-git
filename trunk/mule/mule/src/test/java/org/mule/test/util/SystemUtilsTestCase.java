/*
 * $Id
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the BSD style
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.util;

import java.util.Map;

import junit.framework.TestCase;

import org.mule.util.SystemUtils;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class SystemUtilsTestCase extends TestCase
{

    public void testEnvironment() throws Exception
    {
        Map env = SystemUtils.getenv();
        assertNotNull(env);
        assertFalse(env.isEmpty());
        assertNotNull(env.get("JAVA_HOME"));
        assertEquals(env.get("JAVA_HOME"), SystemUtils.getenv("JAVA_HOME"));
    }

}
