/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.expression;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.expression.ExpressionEvaluator;
import org.mule.api.expression.ExpressionRuntimeException;
import org.mule.config.i18n.CoreMessages;
import org.mule.endpoint.AbstractEndpointBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Looks up information about objects in the Registry
 *
 * @see org.mule.api.expression.ExpressionEvaluator
 * @see org.mule.expression.DefaultExpressionManager
 */
public class RegistryExpressionEvaluator implements ExpressionEvaluator, MuleContextAware
{
    /**
     * logger used by this class
     */
    protected transient final Log logger = LogFactory.getLog(RegistryExpressionEvaluator.class);


    public static final String NAME = "registry";

    private MuleContext muleContext;

    public void setMuleContext(MuleContext context)
    {
        this.muleContext = context;
    }

    public Object evaluate(String expression, MuleMessage message)
    {
        int i = expression.indexOf(".");
        String name;
        String property = null;
        boolean propertyRequired = true;
        boolean objectRequired = true;
        if (i > 0)
        {
            name = expression.substring(0, i);
            property = expression.substring(i + 1);
            if (property.endsWith("*"))
            {
                propertyRequired = false;
                property = property.substring(property.length() - 1);
            }
        }
        else
        {
            name = expression;
        }

        if (name.endsWith("*"))
        {
            objectRequired = false;
            name = name.substring(name.length() - 1);
        }

        Object o = muleContext.getRegistry().lookupObject(name);

        if (o == null && objectRequired)
        {
            throw new ExpressionRuntimeException(CoreMessages.expressionEvaluatorReturnedNull(NAME, expression));
        }
        else if (o == null || property == null)
        {
            return o;
        }
        else if(muleContext.getExpressionManager().isEvaluatorRegistered("bean"))
        {
            //Special handling of Mule object types
            if(o instanceof AbstractEndpointBuilder)
            {
                property = "endpointBuilder.endpoint." + property;
            }
            
            Object p = muleContext.getExpressionManager().evaluate("#[bean:" + property + "]", new DefaultMuleMessage(o, muleContext));
            if (p == null && propertyRequired)
            {
                throw new ExpressionRuntimeException(CoreMessages.expressionEvaluatorReturnedNull(NAME, name + "." + property));
            }
            else
            {
                return p;
            }
        }
        else
        {
            throw new ExpressionRuntimeException(CoreMessages.expressionEvaluatorNotRegistered("bean"));
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name)
    {
        throw new UnsupportedOperationException();
    }
}