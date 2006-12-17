/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extras.spring.config.parsers;

import org.mule.config.MuleConfiguration;
import org.mule.extras.spring.config.AbstractMuleSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * TODO
 */
public class ConfigurationDefinitionParser extends AbstractMuleSingleBeanDefinitionParser
{

    protected Class getBeanClass(Element element)
    {
        return MuleConfiguration.class;
    }
}
