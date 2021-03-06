/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.config;

import org.mule.config.converters.ConnectorConverter;
import org.mule.tck.testmodels.mule.TestConnector;

import org.apache.commons.beanutils.Converter;

public class ConnectorConverterTestCase extends AbstractConverterTestCase
{
    public Converter getConverter()
    {
        return new ConnectorConverter();
    }

    public Object getValidConvertedType()
    {
        return new TestConnector();
    }

    public String getLookupMethod()
    {
        return "lookupConnector";
    }
}
