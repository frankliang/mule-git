/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.examples.loanbroker.transformers;

import org.mule.examples.loanbroker.bank.Bank;
import org.mule.examples.loanbroker.messages.LoanBrokerQuoteRequest;
import org.mule.routing.outbound.StaticRecipientList;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.transformer.TransformerException;

/**
 * Set the Recipient List property on the LoanBrokerQuoteRequest message based on the
 * list of banks in LoanBrokerQuoteRequest.getLenders()
 */
public class SetLendersAsRecipients extends AbstractEventAwareTransformer
{

    public SetLendersAsRecipients()
    {
        this.registerSourceType(LoanBrokerQuoteRequest.class);
        this.setReturnClass(UMOMessage.class);
    }

    public Object transform(Object src, String encoding, UMOEventContext context) throws TransformerException
    {
        Bank[] lenders = ((LoanBrokerQuoteRequest)src).getLenders();

        String recipients = "";
        for (int i = 0; i < lenders.length; i++)
        {
            if (i > 0) recipients += ",";
            recipients += lenders[i].getEndpoint();
        }

        UMOMessage msg = context.getMessage();
        context.getMessage().setProperty(StaticRecipientList.RECIPIENTS_PROPERTY, recipients);
        return msg;
    }

}
