/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.xml.transformer.wire;

import org.mule.module.xml.transformer.ObjectToXml;
import org.mule.module.xml.transformer.XStreamFactory;
import org.mule.module.xml.transformer.XmlToObject;
import org.mule.transformer.wire.TransformerPairWireFormat;

import java.util.List;
import java.util.Map;

/**
 * Serializes objects using XStream. This is equivelent of using the ObjectToXml and
 * XmlToObject except that there is no source or return type checking.
 */
public class XStreamWireFormat extends TransformerPairWireFormat
{
    public XStreamWireFormat() throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        this(XStreamFactory.XSTREAM_XPP_DRIVER, null, null);
    }

    public XStreamWireFormat(String driverClassName, Map aliases, List converters)
        throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        XmlToObject in = new XmlToObject();
        in.setDriverClassName(driverClassName);
        in.setAliases(aliases);
        in.setConverters(converters);
        setInboundTransformer(in);

        ObjectToXml out = new ObjectToXml();
        out.setDriverClassName(driverClassName);
        out.setAliases(aliases);
        out.setConverters(converters);
        // TODO This is currently needed as a workaround for MULE-2881, this needs to
        // be removed is this is not the solution to MULE-2881
        out.setAcceptUMOMessage(true);
        setOutboundTransformer(out);
    }

}
