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

import java.util.Collection;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

/**
 * Processes child property elements in Xml but sets the properties on the parent object.  This is
 * useful when an object has lots of properties and its more readable to break those properties into
 * groups that can be represented as a sub-element in Xml.
 */
public class CompoundElementDefinitionParser  extends AbstractHierarchicalDefinitionParser
{

    public static final String COMPOUND_ELEMENT = "compound";
    
    protected Class getBeanClass(Element element)
    {
        //this has no impact since we just use the bean definition to hold property configurations
        return Object.class;
    }

    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext)
    {
        setRegistry(parserContext.getRegistry());

        parserContext.getContainingBeanDefinition();
        this.parserContext = parserContext;
        Class beanClass = getBeanClass(element);
        Assert.state(beanClass != null, "Class returned from getBeanClass(Element) must not be null, element is: " + element.getNodeName());
        BeanDefinitionBuilder builder = createBeanDefinitionBuilder(element, beanClass);
        builder.setSource(parserContext.extractSource(element));
        if (parserContext.isNested())
        {
            // Inner bean definition must receive same singleton status as containing bean.
            builder.setSingleton(parserContext.getContainingBeanDefinition().isSingleton());
        }
        doParse(element, parserContext, builder);

        BeanDefinition bd = getParentBeanDefinition(element);
        MutablePropertyValues parentProperties = bd.getPropertyValues();
        for (int i=0;i < builder.getBeanDefinition().getPropertyValues().getPropertyValues().length; i++)
        {
            PropertyValue newPropertyValue = builder.getBeanDefinition().getPropertyValues().getPropertyValues()[i];
            String name = newPropertyValue.getName();
            Object value = newPropertyValue.getValue();
            if (!propertyToolkit.isIgnored(name))
            {
                if (propertyToolkit.isCollection(name))
                {
                    Collection values = new ManagedList();
                    if (parentProperties.contains(name))
                    {
                        values = (Collection) parentProperties.getPropertyValue(name).getValue();
                        parentProperties.removePropertyValue(name);
                    }
                    values.add(value);
                    parentProperties.addPropertyValue(name, values);
                }
                else
                {
                    parentProperties.addPropertyValue(name, value);
                }
            }
        }
        
//        AbstractBeanDefinition bd = (AbstractBeanDefinition)parserContext.getContainingBeanDefinition();
        bd.setAttribute(COMPOUND_ELEMENT, Boolean.TRUE);
        return (AbstractBeanDefinition) bd;
    }

}
