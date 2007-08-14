/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.spring.parsers;

import org.mule.config.builders.MuleXmlConfigurationBuilder;
import org.mule.umo.UMOException;
import org.mule.util.StringUtils;

import junit.framework.TestCase;

public class MissingParserTestCase extends TestCase
{

    protected String getConfigResources()
    {
        return "org/mule/config/spring/parsers/missing-parser-test.xml";
    }

    public void testHelpfulErrorMessage() throws UMOException
    {
        try
        {
            MuleXmlConfigurationBuilder builder = new MuleXmlConfigurationBuilder();
            builder.configure(getConfigResources());
        }
        catch (Exception e)
        {
            assertTrue(StringUtils.contains(e.getMessage(), "Is the module or transport"));
        }
    }

}