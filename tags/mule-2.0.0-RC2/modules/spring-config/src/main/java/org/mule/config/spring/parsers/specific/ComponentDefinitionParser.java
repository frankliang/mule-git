/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.spring.parsers.specific;

import org.mule.config.spring.parsers.AbstractMuleBeanDefinitionParser;
import org.mule.config.spring.parsers.MuleDefinitionParser;
import org.mule.config.spring.parsers.PreProcessor;
import org.mule.config.spring.parsers.assembly.configuration.PropertyConfiguration;
import org.mule.config.spring.parsers.delegate.AbstractParallelDelegatingDefinitionParser;
import org.mule.config.spring.util.SpringXMLUtils;
import org.mule.util.StringUtils;
import org.mule.util.object.PooledObjectFactory;

import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ComponentDefinitionParser extends AbstractParallelDelegatingDefinitionParser
{

    private MuleDefinitionParser normalConfig = new ObjectFactoryWrapper("serviceFactory");
    private MuleDefinitionParser shortcutConfig = new ObjectFactoryDefinitionParser(PooledObjectFactory.class, "serviceFactory");

    public ComponentDefinitionParser()
    {
        addDelegate(normalConfig);
        addDelegate(shortcutConfig);
        registerPreProcessor(new CheckExclusiveClassAttributeObjectFactory());
    }

    protected MuleDefinitionParser getDelegate(Element element, ParserContext parserContext)
    {
        if (StringUtils.isEmpty(element.getAttribute(AbstractMuleBeanDefinitionParser.ATTRIBUTE_CLASS)))
        {
            return normalConfig;
        }
        else
        {
            return shortcutConfig;
        }
    }

}

/**
 * Given that the service object-factory is extensible and new object factory types can be implemented and
 * used by substitution, the only extensible way of checking for the existence of an object-factory child
 * element is by exclusion.<br>
 * This pre-processor checks for the existence of a <i>"class"</i> attribute on the service, and throws an
 * exception if the service has any child elements that are not binding's.
 */
class CheckExclusiveClassAttributeObjectFactory implements PreProcessor
{

    private static final String BINDING_CHILD_ELEMENT = "binding";

    public void preProcess(PropertyConfiguration config, Element element)
    {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            String alias = SpringXMLUtils.attributeName((Attr) attributes.item(i));
            if (alias.equals(AbstractMuleBeanDefinitionParser.ATTRIBUTE_CLASS))
            {
                for (int j = 0; j < element.getChildNodes().getLength(); j++)
                {
                    Node child = element.getChildNodes().item(j);
                    if (child instanceof Element &&
                            !child.getLocalName().equals(BINDING_CHILD_ELEMENT))
                    {
                        StringBuffer message = new StringBuffer("The child element '");
                        message.append(child.getLocalName());
                        message.append("' cannot appear with the 'class' attribute");
                        message.append(" in element ");
                        message.append(SpringXMLUtils.elementToString(element));
                        message.append(".");
                        throw new CheckExclusiveClassAttributeObjectFactoryException(message.toString());
                    }
                }

            }
        }
    }

    public static class CheckExclusiveClassAttributeObjectFactoryException extends IllegalStateException
    {

        private static final long serialVersionUID = 4625276914151932111L;

        private CheckExclusiveClassAttributeObjectFactoryException(String message)
        {
            super(message);
        }

    }

}