/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.examples.loanbroker.transformers;

import org.mule.examples.loanbroker.messages.Customer;
import org.mule.examples.loanbroker.messages.CustomerQuoteRequest;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.transformer.TransformerException;

/**
 * Converts parameters on the message into a CustomerQuoteRequest object
 */
public class RestRequestToCustomerRequest extends AbstractEventAwareTransformer
{

    public RestRequestToCustomerRequest()
    {
        setReturnClass(CustomerQuoteRequest.class);
    }

    public Object transform(Object src, String encoding, UMOEventContext context) throws TransformerException
    {
        String name;
        int ssn;
        double amount;
        int duration;

        try
        {
            name = getParam(context, "customerName");
            ssn = Integer.parseInt(getParam(context, "ssn"));
            amount = Double.parseDouble(getParam(context, "loanAmount"));
            duration = Integer.parseInt(getParam(context, "loanDuration"));
        }
        catch (Exception e)
        {
            throw new TransformerException(this, e);
        }

        Customer c = new Customer(name, ssn);
        CustomerQuoteRequest request = new CustomerQuoteRequest(c, amount, duration);
        return request;
    }

    protected String getParam(UMOEventContext context, String name) throws NullPointerException
    {
        String value = context.getMessage().getStringProperty(name, null);
        if (value == null)
        {
            throw new NullPointerException("Parameter '" + name + "' must be set on the request");
        }
        return value;
    }
}
