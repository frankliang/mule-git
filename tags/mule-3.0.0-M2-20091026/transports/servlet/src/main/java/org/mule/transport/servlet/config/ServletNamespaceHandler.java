/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.servlet.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.specific.TransformerDefinitionParser;
import org.mule.endpoint.URIBuilder;
import org.mule.transport.servlet.ServletConnector;
import org.mule.transport.servlet.transformers.HttpRequestToByteArray;
import org.mule.transport.servlet.transformers.HttpRequestToInputStream;
import org.mule.transport.servlet.transformers.HttpRequestToParameterMap;

/**
 * Registers a Bean Definition Parser for handling <code><servlet:*></code> elements.
 */
public class ServletNamespaceHandler extends AbstractMuleNamespaceHandler
{
    public void init()
    {
        registerStandardTransportEndpoints(ServletConnector.SERVLET, URIBuilder.PATH_ATTRIBUTES);
        registerConnectorDefinitionParser(ServletConnector.class);
        registerBeanDefinitionParser("http-request-to-parameter-map", new TransformerDefinitionParser(HttpRequestToParameterMap.class));
        registerBeanDefinitionParser("http-request-to-input-stream", new TransformerDefinitionParser(HttpRequestToInputStream.class));
        registerBeanDefinitionParser("http-request-to-byte-array", new TransformerDefinitionParser(HttpRequestToByteArray.class));        
    }
}
