/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.example.bookstore.transformers;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.example.bookstore.Book;
import org.mule.example.bookstore.BookstoreAdminMessages;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.mule.util.StringUtils;

/**
 * Transforms a Map of HttpRequest parameters into a Book object.  
 * The request parameters are always strings (they come from the HTML form), 
 * so we need to parse and convert them to their appropriate types.
 */
public class HttpRequestToBook extends AbstractMessageAwareTransformer
{
    public HttpRequestToBook()
    {
        super();
        registerSourceType(Object.class);
        setReturnClass(Book.class);
    }

    @Override
    public Object transform(MuleMessage message, String outputEncoding) throws TransformerException
    {
        String author = message.getStringProperty("author", null);
        String title = message.getStringProperty("title", null);
        String price = message.getStringProperty("price", null);

        if (StringUtils.isBlank(author))
        {
            throw new TransformerException(BookstoreAdminMessages.missingAuthor());
        }
        if (StringUtils.isBlank(title))
        {
            throw new TransformerException(BookstoreAdminMessages.missingTitle());
        }
        if (StringUtils.isBlank(price))
        {
            throw new TransformerException(BookstoreAdminMessages.missingPrice());
        }

        return new Book(author, title, Double.parseDouble(price));
    }
}
