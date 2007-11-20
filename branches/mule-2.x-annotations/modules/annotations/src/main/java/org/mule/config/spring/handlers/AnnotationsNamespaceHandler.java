/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.spring.handlers;

import org.mule.config.spring.parser.specific.AnnotatedPojoComponentDefinitionParser;
import org.mule.config.spring.parser.specific.AnnotatedServiceDefinitionParser;

/**
 * The Namespace handler used to register parsers for elements in the 'annotations' namespace
 */
public class AnnotationsNamespaceHandler extends AbstractIgnorableNamespaceHandler
{

    public void init()
    {
        registerBeanDefinitionParser("service", new AnnotatedServiceDefinitionParser());
        registerBeanDefinitionParser("component", new AnnotatedPojoComponentDefinitionParser());
        //TODO registerBeanDefinitionParser("annotated-entrypoint-resolver", new ChildDefinitionParser("filter", IsXmlFilter.class));
    }
}
