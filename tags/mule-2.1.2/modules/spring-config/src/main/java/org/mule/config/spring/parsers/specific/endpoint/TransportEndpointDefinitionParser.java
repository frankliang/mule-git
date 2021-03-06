/*
 * $Id:TransportEndpointDefinitionParser.java 8321 2007-09-10 19:22:52Z acooke $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.spring.parsers.specific.endpoint;

import org.mule.config.spring.parsers.specific.endpoint.support.AddressedEndpointDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.support.ChildEndpointDefinitionParser;
import org.mule.endpoint.URIBuilder;

/**
 * This is intended for use by endpoint-specific parsers for non-global endpoint
 * elements.
 *
 * <p>It generates both an endpoint (which should subclass
 * {@link org.mule.api.endpoint.ImmutableEndpoint}) and a
 * {@link org.mule.endpoint.URIBuilder}.  The URI is
 * then injected into the endpoint.  So the associated schema can enable any of the
 * suitable {@link org.mule.endpoint.URIBuilder# ALL_ATTRIBUTES}
 * or add appropriate mappings.
 */
public class TransportEndpointDefinitionParser extends AddressedEndpointDefinitionParser
{

    public TransportEndpointDefinitionParser(String protocol, Class endpoint, String[] requiredAddressAttributes)
    {
        this(protocol, PROTOCOL, endpoint, requiredAddressAttributes);
    }

    public TransportEndpointDefinitionParser(String metaOrProtocol, boolean isMeta, Class endpoint,
                                             String[] requiredAddressAttributes)
    {
        this(metaOrProtocol, isMeta, endpoint, requiredAddressAttributes, new String[]{});
    }

    /**
     * @param metaOrProtocol The transport metaOrProtocol ("tcp" etc)
     * @param isMeta Whether transport is "meta" or not (eg cxf)
     * @param endpoint The endpoint class to construct
     * @param requiredAddressAttributes A list of attribute names that are required if "address"
     * isn't present
     * @param requiredProperties A list of property names that are required if "address" isn't present
     */
    public TransportEndpointDefinitionParser(String metaOrProtocol, boolean isMeta, Class endpoint,
                                             String[] requiredAddressAttributes, String[] requiredProperties)
    {
        super(metaOrProtocol, isMeta, new ChildEndpointDefinitionParser(endpoint),
                requiredAddressAttributes, requiredProperties);
    }

    public TransportEndpointDefinitionParser(String metaOrProtocol, boolean isMeta, Class endpoint,
                                             String[] endpointAttributes,
                                             String[][] requiredAddressAttributes,
                                             String[][] requiredProperties)
    {
        this(metaOrProtocol, isMeta, endpoint, endpointAttributes, URIBuilder.ALL_ATTRIBUTES,
                requiredAddressAttributes, requiredProperties);
    }

    public TransportEndpointDefinitionParser(String metaOrProtocol, boolean isMeta, Class endpoint,
                                             String[] endpointAttributes,
                                             String[] addressAttributes,
                                             String[][] requiredAddressAttributes,
                                             String[][] requiredProperties)
    {
        super(metaOrProtocol, isMeta, new ChildEndpointDefinitionParser(endpoint),
                endpointAttributes, addressAttributes, requiredAddressAttributes, requiredProperties);
    }

}
