/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformers.xml;

import org.mule.api.transformer.Transformer;
import org.mule.util.IOUtils;

import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.w3c.dom.Document;

public class DomXmlTransformersTestCase extends AbstractXmlTransformerTestCase
{

    private String srcData;
    private Document resultData;

    // @Override
    protected void doSetUp() throws Exception
    {
        srcData = IOUtils.getResourceAsString("cdcatalog.xml", getClass());
        org.dom4j.Document dom4jDoc = DocumentHelper.parseText(srcData);
        resultData = new DOMWriter().write(dom4jDoc);
    }

    public Transformer getTransformer() throws Exception
    {
        XmlToDomDocument trans = new XmlToDomDocument();
        trans.setReturnClass(org.w3c.dom.Document.class);
        return trans;
    }

    public Transformer getRoundTripTransformer() throws Exception
    {
        DomDocumentToXml trans = new DomDocumentToXml();
        trans.setReturnClass(String.class);
        return trans;
    }

    public Object getTestData()
    {
        return srcData;
    }

    public Object getResultData()
    {
        return resultData;
    }

}
