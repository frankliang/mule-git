/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformers.xml;

import org.mule.umo.transformer.TransformerException;

/**
 * <code>DomDocumentToXml</code> Transform a org.w3c.dom.Document to XML String
 */
public class DomDocumentToXml extends AbstractXmlTransformer
{

    public DomDocumentToXml()
    {
        setReturnClass(String.class);
    }

    public Object doTransform(Object src, String encoding) throws TransformerException
    {
        try
        {
            // We now offer XML in byte OR String form.
            // String remains the default like before.
            if (byte[].class.equals(returnClass))
                return convertToBytes(src, encoding);
            else
                return convertToText(src, encoding);
        }
        catch (Exception e)
        {
            throw new TransformerException(this, e);
        }
    }

}
