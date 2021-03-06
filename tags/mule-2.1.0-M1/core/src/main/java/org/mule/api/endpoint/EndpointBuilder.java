/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.endpoint;

import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.routing.filter.Filter;
import org.mule.api.security.EndpointSecurityFilter;
import org.mule.api.transaction.TransactionConfig;
import org.mule.api.transformer.Transformer;
import org.mule.api.transport.ConnectionStrategy;
import org.mule.api.transport.Connector;
import org.mule.endpoint.URIBuilder;

import java.util.List;
import java.util.Map;

/**
 * Constructs endpoints. Transport specific endpoints can easily resolve the Endpoint implementation to be
 * uses, for generic endpoints we can either resolve the transport from uri string or use a default
 * implementation.
 */
public interface EndpointBuilder extends MuleContextAware, Cloneable
{

    /**
     * Constructs inbound endpoints
     * 
     * @return
     * @throws EndpointException
     * @throws InitialisationException
     */
    InboundEndpoint buildInboundEndpoint() throws EndpointException, InitialisationException;

    /**
     * Constructs outbound endpoints
     * 
     * @return
     * @throws EndpointException
     * @throws InitialisationException
     */
    OutboundEndpoint buildOutboundEndpoint() throws EndpointException, InitialisationException;

    void setConnector(Connector connector);

    void addTransformer(Transformer transformer);

    void setTransformers(List transformers);

    void setResponseTransformers(List responseTransformer);

    void setName(String name);

    void setProperty(Object key, Object value);
    
    void setProperties(Map properties);

    void setTransactionConfig(TransactionConfig transactionConfig);

    void setFilter(Filter filter);

    void setDeleteUnacceptedMessages(boolean deleteUnacceptedMessages);

    void setSecurityFilter(EndpointSecurityFilter securityFilter);

    void setSynchronous(boolean synchronous);

    void setRemoteSync(boolean remoteSync);

    void setRemoteSyncTimeout(int remoteSyncTimeout);

    void setInitialState(String initialState);

    void setEncoding(String encoding);

    void setRegistryId(String registryId);

    void setMuleContext(MuleContext muleContext);

    void setConnectionStrategy(ConnectionStrategy connectionStrategy);

    void setURIBuilder(URIBuilder URIBuilder);

    Object clone() throws CloneNotSupportedException;

}
