/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.endpoint;

import org.mule.RegistryContext;
import org.mule.api.MuleContext;
import org.mule.api.DefaultMuleException;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.EndpointException;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.registry.ServiceDescriptorFactory;
import org.mule.api.registry.ServiceException;
import org.mule.api.routing.filter.Filter;
import org.mule.api.security.EndpointSecurityFilter;
import org.mule.api.transaction.TransactionConfig;
import org.mule.api.transformer.Transformer;
import org.mule.api.transport.ConnectionStrategy;
import org.mule.api.transport.Connector;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.Message;
import org.mule.transaction.MuleTransactionConfig;
import org.mule.transformer.TransformerUtils;
import org.mule.transport.AbstractConnector;
import org.mule.transport.SingleAttemptConnectionStrategy;
import org.mule.transport.service.TransportFactory;
import org.mule.transport.service.TransportFactoryException;
import org.mule.transport.service.TransportServiceDescriptor;
import org.mule.util.ClassUtils;
import org.mule.util.MapCombiner;
import org.mule.util.ObjectNameHelper;
import org.mule.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Abstract endpoint builder used for externalizing the complex creation logic of endpoints out of the
 * endpoint instance itself. <br/> The use of a builder allows i) Endpoints to be configured once and created
 * in a repeatable fashion (global endpoints), ii) Allow for much more extensibility in endpoint creation for
 * transport specific endpoints, streaming endpoints etc.<br/>

 */
public abstract class AbstractEndpointBuilder implements EndpointBuilder
{
 
    public static final int GET_OR_CREATE_CONNECTOR = 0;
    public static final int ALWAYS_CREATE_CONNECTOR = 1;

    public static final int NEVER_CREATE_CONNECTOR = 2;
    public static final int USE_CONNECTOR = 3;

    protected URIBuilder uriBuilder;
    protected Connector connector;
    protected List transformers = TransformerUtils.UNDEFINED;
    protected List responseTransformers = TransformerUtils.UNDEFINED;
    protected String name;
    protected Map properties = new HashMap();
    protected TransactionConfig transactionConfig;
    protected Filter filter;
    protected Boolean deleteUnacceptedMessages;
    protected EndpointSecurityFilter securityFilter;
    protected Boolean synchronous;
    protected Boolean remoteSync;
    protected Integer remoteSyncTimeout;
    protected String initialState = ImmutableEndpoint.INITIAL_STATE_STARTED;
    protected String encoding;
    protected Integer createConnector;
    protected ConnectionStrategy connectionStrategy;

    // not included in equality/hash
    protected String registryId = null;
    protected MuleContext muleContext;

    public ImmutableEndpoint buildInboundEndpoint() throws EndpointException, InitialisationException
    {
        return doBuildInboundEndpoint();
    }

    public ImmutableEndpoint buildOutboundEndpoint() throws EndpointException, InitialisationException
    {
        return doBuildOutboundEndpoint();
    }

    protected void configureEndpoint(MuleEndpoint ep) throws InitialisationException, EndpointException
    {
        // protected String registryId = null; ??
        EndpointURI endpointURI = uriBuilder.getEndpoint();
        endpointURI.initialise();
        ep.setEndpointURI(endpointURI);
        Connector connector = getConnector();
        ep.setConnector(connector);

        // Do not inherit from connector
        ep.setSecurityFilter(getSecurityFilter());
        ep.setTransactionConfig(getTransactionConfig());
        ep.setProperties(getProperties());
        ep.setName(getName(ep));

        // Can inherit from connector
        ep.setConnectionStrategy(getConnectionStrategy(connector));
        ep.setDeleteUnacceptedMessages(getDeleteUnacceptedMessages(connector));
        ep.setEncoding(getEndpointEncoding(connector));
        ep.setFilter(getFilter(connector));
        ep.setInitialState(getInitialState(connector));
        ep.setRemoteSyncTimeout(getRemoteSyncTimeout(connector));

        boolean remoteSync = getRemoteSync(connector);
        ep.setRemoteSync(remoteSync);
        if (remoteSync)
        {
            ep.setSynchronous(true);
        }
        else
        {
            ep.setSynchronous(getSynchronous(connector, ep));
        }
        ep.setMuleContext(muleContext);
    }

    protected ImmutableEndpoint doBuildInboundEndpoint() throws InitialisationException, EndpointException
    {
        InboundEndpoint ep = new InboundEndpoint();
        configureEndpoint(ep);
        ep.setTransformers(getInboundTransformers(ep.getConnector(), ep.getEndpointURI()));
        ep.setResponseTransformers(getResponseTransformers(ep.getConnector(), ep.getEndpointURI()));
        return ep;
    }

    protected ImmutableEndpoint doBuildOutboundEndpoint() throws InitialisationException, EndpointException
    {
        OutboundEndpoint ep = new OutboundEndpoint();
        configureEndpoint(ep);
        ep.setTransformers(getOutboundTransformers(ep.getConnector(), ep.getEndpointURI()));
        ep.setResponseTransformers(getResponseTransformers(ep.getConnector(), ep.getEndpointURI()));
        return ep;
    }

    protected boolean getSynchronous(Connector connector, ImmutableEndpoint endpoint)
    {
        return synchronous != null ? synchronous.booleanValue() : getDefaultSynchronous(connector, endpoint);
    } 

    protected boolean getDefaultSynchronous(Connector connector, ImmutableEndpoint endpoint)
    {
        if (connector != null && connector.isSyncEnabled(endpoint))
        {
            return true;
        }
        else
        {
            return RegistryContext.getConfiguration().isDefaultSynchronousEndpoints();
        }
    }
    
    protected ConnectionStrategy getConnectionStrategy(Connector connector)
    {
        return connectionStrategy != null ? connectionStrategy : getDefaultConnectionStrategy(connector);
    }

    protected ConnectionStrategy getDefaultConnectionStrategy(Connector connector)
    {
        return new SingleAttemptConnectionStrategy();
    }

    protected TransactionConfig getTransactionConfig()
    {
        return transactionConfig != null ? transactionConfig : getDefaultTransactionConfig();
    }

    protected TransactionConfig getDefaultTransactionConfig()
    {
        return new MuleTransactionConfig();
    }

    protected EndpointSecurityFilter getSecurityFilter()
    {
        return securityFilter != null ? securityFilter : getDefaultSecurityFilter();
    }

    protected EndpointSecurityFilter getDefaultSecurityFilter()
    {
        return null;
    }

    protected Connector getConnector() throws EndpointException
    {
        return connector != null ? connector : getDefaultConnector();
    }

    protected Connector getDefaultConnector() throws EndpointException
    {
        return getConnector(uriBuilder.getEndpoint(), muleContext);
    }

    protected String getName(ImmutableEndpoint endpoint)
    {
        // uriBuilder cannot return an endpoint with an endpoint name (which is deprecated anyway)
        // so we don't use it here
//        String uriName = uriBuilder.getEndpoint().getEndpointName();
        return name != null ? name : ObjectNameHelper.getEndpointName(endpoint);
    }

    protected Map getProperties()
    {
        // Add properties from builder, endpointURI and then seal (make unmodifiable)
        LinkedList maps = new LinkedList();
        // properties from url come first
        if (null != uriBuilder)
        {
            // properties from the URI itself
            maps.addLast(uriBuilder.getEndpoint().getParams());
        }
        // properties on builder may override url
        if (properties != null)
        {
            maps.addLast(properties);
        }
        MapCombiner combiner = new MapCombiner();
        combiner.setList(maps);
        return Collections.unmodifiableMap(combiner);
    }

    protected boolean getRemoteSync(Connector connector)
    {
        return remoteSync != null ? remoteSync.booleanValue() : getDefaultRemoteSync(connector);

    }

    protected boolean getDefaultRemoteSync(Connector connector)
    {
        // what is this for?!
        if (connector == null || connector.isRemoteSyncEnabled())
        {
            return false;
        }
        else
        {
            return false;
        }
    }

    protected boolean getDeleteUnacceptedMessages(Connector connector)
    {
        return deleteUnacceptedMessages != null
                                               ? deleteUnacceptedMessages.booleanValue()
                                               : getDefaultDeleteUnacceptedMessages(connector);

    }

    protected boolean getDefaultDeleteUnacceptedMessages(Connector connector)
    {
        return false;
    }

    protected String getEndpointEncoding(Connector connector)
    {
        return encoding != null ? encoding : getDefaultEndpointEncoding(connector);
    }

    protected String getDefaultEndpointEncoding(Connector connector)
    {
        if (muleContext != null)
        {
            return muleContext.getRegistry().getConfiguration().getDefaultEncoding();
        }
        else
        {
            return System.getProperty("file.encoding");
        }
    }

    protected Filter getFilter(Connector connector)
    {
        return filter != null ? filter : getDefaultFilter(connector);

    }

    protected Filter getDefaultFilter(Connector connector)
    {
        return null;
    }

    protected String getInitialState(Connector connector)
    {
        return initialState != null ? initialState : getDefaultInitialState(connector);

    }

    protected String getDefaultInitialState(Connector connector)
    {
        return ImmutableEndpoint.INITIAL_STATE_STARTED;
    }

    protected int getRemoteSyncTimeout(Connector connector)
    {
        return remoteSyncTimeout != null ? remoteSyncTimeout.intValue() : getDefaultRemoteSyncTimeout(connector);

    }

    protected int getDefaultRemoteSyncTimeout(Connector connector)
    {
        return muleContext.getRegistry().getConfiguration().getDefaultSynchronousEventTimeout();
    }

    protected List getInboundTransformers(Connector connector, EndpointURI endpointURI)
        throws TransportFactoryException
    {
        // #1 Transformers set on builder
        if (TransformerUtils.isDefined(transformers))
        {
            return transformers;
        }

        // #2 Transformer specified on uri
        List transformers = getTransformersFromString(endpointURI.getTransformers());
        if (TransformerUtils.isDefined(transformers))
        {
            return transformers;
        }

        // #3 Default Transformer
        return getDefaultInboundTransformers(connector);
    }

    protected List getDefaultInboundTransformers(Connector connector) throws TransportFactoryException
    {
        try
        {
            return TransformerUtils.getDefaultInboundTransformers(getNonNullServiceDescriptor(
                uriBuilder.getEndpoint().getSchemeMetaInfo(), getOverrides(connector)));
        }
        catch (Exception e)
        {
            throw new TransportFactoryException(e);
        }
    }

    protected List getOutboundTransformers(Connector connector, EndpointURI endpointURI)
        throws TransportFactoryException
    {
        // #1 Transformers set on builder
        if (TransformerUtils.isDefined(transformers))
        {
            return transformers;
        }

        // #2 Transformer specified on uri
        List transformers = getTransformersFromString(endpointURI.getTransformers());
        if (TransformerUtils.isDefined(transformers))
        {
            return transformers;
        }

        // #3 Default Transformer
        return getDefaultOutboundTransformers(connector);
    }

    protected List getDefaultOutboundTransformers(Connector connector) throws TransportFactoryException
    {
        try
        {
            return TransformerUtils.getDefaultOutboundTransformers(getNonNullServiceDescriptor(
                uriBuilder.getEndpoint().getSchemeMetaInfo(), getOverrides(connector)));
        }
        catch (Exception e)
        {
            throw new TransportFactoryException(e);
        }
    }

    protected List getResponseTransformers(Connector connector, EndpointURI endpointURI)
        throws TransportFactoryException
    {
        // #1 Transformers set on builder
        if (TransformerUtils.isDefined(responseTransformers))
        {
            return responseTransformers;
        }

        // #2 Transformer specified on uri
        List transformers = getTransformersFromString(endpointURI.getResponseTransformers());
        if (TransformerUtils.isDefined(transformers))
        {
            return transformers;
        }

        // #3 Default Transformer
        return getDefaultResponseTransformers(connector);
    }

    protected List getDefaultResponseTransformers(Connector connector) throws TransportFactoryException
    {
        try
        {
            return TransformerUtils.getDefaultResponseTransformers(getNonNullServiceDescriptor(
                uriBuilder.getEndpoint().getSchemeMetaInfo(), getOverrides(connector)));
        }
        catch (Exception e)
        {
            throw new TransportFactoryException(e);
        }
    }

    private List getTransformersFromString(String transformers) throws TransportFactoryException
    {
        try
        {
            return TransformerUtils.getTransformers(transformers);
        }
        catch (DefaultMuleException e)
        {
            throw new TransportFactoryException(e);
        }
    }

    private Properties getOverrides(Connector connector)
    {
        // Get connector specific overrides to set on the descriptor
        Properties overrides = new Properties();
        if (connector instanceof AbstractConnector)
        {
            Map so = ((AbstractConnector) connector).getServiceOverrides();
            if (so != null)
            {
                overrides.putAll(so);
            }
        }
        return overrides;
    }

    private TransportServiceDescriptor getNonNullServiceDescriptor(String scheme, Properties overrides)
        throws ServiceException
    {
        TransportServiceDescriptor sd = (TransportServiceDescriptor) RegistryContext.getRegistry()
            .lookupServiceDescriptor(ServiceDescriptorFactory.PROVIDER_SERVICE_TYPE, scheme, overrides);
        if (null != sd)
        {
            return sd;
        }
        else
        {
            throw new ServiceException(CoreMessages.noServiceTransportDescriptor(scheme));
        }
    }

    private Connector getConnector(EndpointURI endpointURI, MuleContext muleContext)
        throws EndpointException
    {
        String scheme = uriBuilder.getEndpoint().getFullScheme();
        Connector connector;
        try
        {
            if (uriBuilder.getEndpoint().getConnectorName() != null)
            {
                connector = muleContext.getRegistry().lookupConnector(uriBuilder.getEndpoint().getConnectorName());
                if (connector == null)
                {
                    throw new TransportFactoryException(CoreMessages.objectNotRegistered("Connector",
                        uriBuilder.getEndpoint().getConnectorName()));
                }
            }
            else
            {
                connector = TransportFactory.getConnectorByProtocol(scheme);
                if (connector == null)
                {
                    connector = TransportFactory.createConnector(endpointURI, muleContext);
                    muleContext.getRegistry().registerConnector(connector);
                }
            }
        }
        catch (Exception e)
        {
            throw new TransportFactoryException(e);
        }

        if (connector == null)
        {
            Message m = CoreMessages.failedToCreateObjectWith("Endpoint", "endpointURI: " + endpointURI);
            m.setNextMessage(CoreMessages.objectIsNull("connector"));
            throw new TransportFactoryException(m);

        }
        return connector;
    }

    // Builder setters

    public void setConnector(Connector connector)
    {
        this.connector = connector;

    }
    
    public void addTransformer(Transformer transformer)
    {
        if (transformers == TransformerUtils.UNDEFINED)
        {
            transformers = new ArrayList();
        }
        transformers.add(transformer);
    }

    public void setTransformers(List transformers)
    {
        this.transformers = transformers;
    }

    public void setResponseTransformers(List responseTransformers)
    {
        this.responseTransformers = responseTransformers;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * NOTE - this appends properties.
     */
    public void setProperties(Map properties)
    {
        if (null == this.properties)
        {
            this.properties = new HashMap();
        }
        this.properties.putAll(properties);
    }

    /**
     * Sets a property on the endpoint
     *
     * @param key the property key
     * @param value the value of the property
     */
    public void setProperty(Object key, Object value)
    {
        properties.put(key, value);
    }

    public void setTransactionConfig(TransactionConfig transactionConfig)
    {
        this.transactionConfig = transactionConfig;

    }

    public void setFilter(Filter filter)
    {
        this.filter = filter;

    }

    public void setDeleteUnacceptedMessages(boolean deleteUnacceptedMessages)
    {
        this.deleteUnacceptedMessages = new Boolean(deleteUnacceptedMessages);

    }

    public void setSecurityFilter(EndpointSecurityFilter securityFilter)
    {
        this.securityFilter = securityFilter;

    }

    public void setSynchronous(boolean synchronous)
    {
        this.synchronous = new Boolean(synchronous);

    }

    public void setRemoteSync(boolean remoteSync)
    {
        this.remoteSync = new Boolean(remoteSync);

    }

    public void setRemoteSyncTimeout(int remoteSyncTimeout)
    {
        this.remoteSyncTimeout = new Integer(remoteSyncTimeout);

    }

    public void setInitialState(String initialState)
    {
        this.initialState = initialState;

    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;

    }

    public void setCreateConnector(int createConnector)
    {
        this.createConnector = new Integer(createConnector);

    }

    public void setRegistryId(String registryId)
    {
        this.registryId = registryId;

    }

    public void setMuleContext(MuleContext muleContext)
    {
        this.muleContext = muleContext;

    }

    public void setConnectionStrategy(ConnectionStrategy connectionStrategy)
    {
        this.connectionStrategy = connectionStrategy;

    }

    public URIBuilder getEndpointBuilder()
    {
        return uriBuilder;
    }

    public void setURIBuilder(URIBuilder URIBuilder)
    {
        this.uriBuilder = URIBuilder;

    }
    
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((connectionStrategy == null) ? 0 : connectionStrategy.hashCode());
        result = prime * result + ((connector == null) ? 0 : connector.hashCode());
        result = prime * result + ((createConnector == null) ? 0 : createConnector.hashCode());
        result = prime * result + ((deleteUnacceptedMessages == null) ? 0 : deleteUnacceptedMessages.hashCode());
        result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
        result = prime * result + ((uriBuilder == null) ? 0 : uriBuilder.getEndpoint().hashCode());
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        result = prime * result + ((initialState == null) ? 0 : initialState.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((remoteSync == null) ? 0 : remoteSync.hashCode());
        result = prime * result + ((remoteSyncTimeout == null) ? 0 : remoteSyncTimeout.hashCode());
        result = prime * result + ((responseTransformers == null) ? 0 : responseTransformers.hashCode());
        result = prime * result + ((securityFilter == null) ? 0 : securityFilter.hashCode());
        result = prime * result + ((synchronous == null) ? 0 : synchronous.hashCode());
        result = prime * result + ((transactionConfig == null) ? 0 : transactionConfig.hashCode());
        result = prime * result + ((transformers == null) ? 0 : transformers.hashCode());
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final AbstractEndpointBuilder other = (AbstractEndpointBuilder) obj;
        return equal(connectionStrategy, other.connectionStrategy)
                && equal(connector, other.connector)
                && equal(createConnector, other.createConnector)
                && equal(deleteUnacceptedMessages, other.deleteUnacceptedMessages)
                && equal(encoding, other.encoding)
                && equal(uriBuilder, other.uriBuilder)
                && equal(filter, other.filter)
                && equal(initialState, other.initialState)
                && equal(name, other.name)
                && equal(properties, other.properties)
                && equal(remoteSync, other.remoteSync)
                && equal(remoteSyncTimeout, other.remoteSyncTimeout)
                && equal(responseTransformers, other.responseTransformers)
                && equal(securityFilter, other.securityFilter)
                && equal(synchronous, other.synchronous)
                && equal(transactionConfig, other.transactionConfig)
                && equal(transformers, other.transformers);
    }

    protected static boolean equal(Object a, Object b)
    {
        return ClassUtils.equal(a, b);
    }

    public Object clone() throws CloneNotSupportedException
    {
        EndpointBuilder builder = (EndpointBuilder) super.clone();
        builder.setConnector(connector);
        builder.setURIBuilder(uriBuilder);
        builder.setTransformers(transformers);
        builder.setResponseTransformers(responseTransformers);
        builder.setName(name);
        builder.setProperties(properties);
        builder.setTransactionConfig(transactionConfig);
        builder.setFilter(filter);
        builder.setSecurityFilter(securityFilter);
        builder.setInitialState(initialState);
        builder.setEncoding(encoding);
        builder.setRegistryId(registryId);
        builder.setMuleContext(muleContext);
        builder.setConnectionStrategy(connectionStrategy);

        if (deleteUnacceptedMessages != null)
        {
            builder.setDeleteUnacceptedMessages(deleteUnacceptedMessages.booleanValue());
        }
        if (synchronous != null)
        {
            builder.setSynchronous(synchronous.booleanValue());
        }
        if (remoteSync != null)
        {
            builder.setRemoteSync(remoteSync.booleanValue());
        }
        if (remoteSyncTimeout != null)
        {
            builder.setRemoteSyncTimeout(remoteSyncTimeout.intValue());
        }

        return builder;
    }
    
}
