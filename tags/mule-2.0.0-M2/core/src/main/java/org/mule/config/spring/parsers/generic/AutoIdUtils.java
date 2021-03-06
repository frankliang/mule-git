/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.spring.parsers.generic;

import org.mule.config.spring.parsers.AbstractMuleBeanDefinitionParser;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import org.w3c.dom.Element;

public class AutoIdUtils
{

    public static final String ATTRIBUTE_ID = AbstractMuleBeanDefinitionParser.ATTRIBUTE_ID;
    public static final String ATTRIBUTE_NAME = AbstractMuleBeanDefinitionParser.ATTRIBUTE_NAME;
    private static final AtomicInteger counter = new AtomicInteger(0);
    public static final String PREFIX = "org.mule.autogen.";

    public static void ensureUniqueId(Element element, String type)
    {
        if (null != element && !element.hasAttribute(ATTRIBUTE_ID))
        {
            if (!element.hasAttribute(ATTRIBUTE_NAME))
            {
                element.setAttribute(ATTRIBUTE_ID, PREFIX + type + "." + counter.incrementAndGet());
            }
            else
            {
                element.setAttribute(ATTRIBUTE_ID, element.getAttribute(ATTRIBUTE_NAME));
            }
        }
    }

    public static void ensureUniqueName(Element element, String type)
    {
        if (null != element && !element.hasAttribute(ATTRIBUTE_NAME))
        {
            element.setAttribute(ATTRIBUTE_NAME, PREFIX + type + "." + counter.incrementAndGet());
        }
    }

    public static void forceUniqueId(Element element, String type)
    {
        if (null != element)
        {
            String id = PREFIX + type + "." + counter.incrementAndGet();
            element.setAttribute(ATTRIBUTE_ID, id);
            element.setAttribute(ATTRIBUTE_NAME, id);
        }
    }

}
