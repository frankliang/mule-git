/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.cxf.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.specific.ComponentDefinitionParser;
import org.mule.transport.cxf.CxfConnector;
import org.mule.transport.cxf.CxfConstants;
import org.mule.transport.cxf.component.WebServiceWrapperComponent;
import org.mule.transport.cxf.support.StaxFeature;

import org.apache.cxf.configuration.spring.SimpleBeanDefinitionParser;

public class CxfNamespaceHandler extends AbstractMuleNamespaceHandler
{

    public void init()
    {
        registerMetaTransportEndpoints(CxfConnector.CXF);

        registerConnectorDefinitionParser(CxfConnector.class);

        registerBeanDefinitionParser("features", new EndpointChildDefinitionParser("features"));

        registerBeanDefinitionParser(CxfConstants.DATA_BINDING, new EndpointChildDefinitionParser(
            CxfConstants.DATA_BINDING));
        
        registerBeanDefinitionParser(CxfConstants.IN_INTERCEPTORS, new EndpointChildDefinitionParser(
            CxfConstants.IN_INTERCEPTORS));

        registerBeanDefinitionParser(CxfConstants.IN_FAULT_INTERCEPTORS, new EndpointChildDefinitionParser(
            CxfConstants.IN_FAULT_INTERCEPTORS));

        registerBeanDefinitionParser(CxfConstants.OUT_INTERCEPTORS, new EndpointChildDefinitionParser(
            CxfConstants.OUT_INTERCEPTORS));

        registerBeanDefinitionParser(CxfConstants.OUT_FAULT_INTERCEPTORS, new EndpointChildDefinitionParser(
            CxfConstants.OUT_FAULT_INTERCEPTORS));
        
        registerBeanDefinitionParser("stax", new SimpleBeanDefinitionParser(StaxFeature.class));
        
        registerBeanDefinitionParser("wrapper-component", new ComponentDefinitionParser(WebServiceWrapperComponent.class));
    }
}
