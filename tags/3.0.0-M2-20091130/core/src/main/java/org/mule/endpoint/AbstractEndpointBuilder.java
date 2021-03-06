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

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.MuleRuntimeException;
import org.mule.api.config.MuleProperties;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.EndpointException;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.registry.ServiceDescriptorFactory;
import org.mule.api.registry.ServiceException;
import org.mule.api.retry.RetryPolicyTemplate;
import org.mule.api.routing.filter.Filter;
import org.mule.api.security.EndpointSecurityFilter;
import org.mule.api.transaction.TransactionConfig;
import org.mule.api.transformer.Transformer;
import org.mule.api.transport.Connector;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.Message;
import org.mule.transaction.MuleTransactionConfig;
import org.mule.transformer.TransformerUtils;
import org.mule.transport.AbstractConnector;
import org.mule.transport.service.TransportFactory;
import org.mule.transport.service.TransportFactoryException;
import org.mule.transport.service.TransportServiceDescriptor;
import org.mule.util.ClassUtils;
import org.mule.util.MapCombiner;
import org.mule.util.ObjectNameHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Abstract endpoint builder used for externalizing the complex creation logic of
 * endpoints out of the endpoint instance itself. <br/> The use of a builder allows
 * i) Endpoints to be configured once and created in a repeatable fashion (global
 * endpoints), ii) Allow for much more extensibility in endpoint creation for
 * transport specific endpoints, streaming endpoints etc.<br/>
 */
public abstract class AbstractEndpointBuilder implements EndpointBuilder
{

    public static final String PROPERTY_RESPONSE_TIMEOUT = "responseTimeout";
    public static final String PROPERTY_RESPONSE_PROPERTIES = "responseProperties";

    protected URIBuilder uriBuilder;
    protected Connector connector;
    protected List<Transformer> transformers;
    protected List<Transformer> responseTransformers;
    protected String name;
    protected Map properties = new HashMap();
    protected TransactionConfig transactionConfig;
    protected Filter filter;
    protected Boolean deleteUnacceptedMessages;
    protected EndpointSecurityFilter securityFilter;
    protected Boolean synchronous;
    protected Integer responseTimeout;
    protected String initialState = ImmutableEndpoint.INITIAL_STATE_STARTED;
    protected String encoding;
    protected Integer createConnector;
    protected RetryPolicyTemplate retryPolicyTemplate;
    protected String responsePropertiesList;

    // not included in equality/hash
    protected String registryId = null;
    protected MuleContext muleContext;

    public InboundEndpoint buildInboundEndpoint() throws EndpointException, InitialisationException
    {
        return doBuildInboundEndpoint();
    }

    public OutboundEndpoint buildOutboundEndpoint() throws EndpointException, InitialisationException
    {
        return doBuildOutboundEndpoint();
    }

    protected void setPropertiesFromProperties(Map properties)
    {
        synchronous = getBooleanProperty(properties, MuleProperties.SYNCHRONOUS_PROPERTY, synchronous);
        responseTimeout = getIntegerProperty(properties, PROPERTY_RESPONSE_TIMEOUT, responseTimeout);
        responsePropertiesList = (String) properties.get(PROPERTY_RESPONSE_PROPERTIES);
    }

    public static Boolean getBooleanProperty(Map properties, String name, Boolean dflt)
    {
        if (properties.containsKey(name))
        {
            return Boolean.valueOf((String) properties.get(name));
        }
        else
        {
            return dflt;
        }
    }

    public static Integer getIntegerProperty(Map properties, String name, Integer dflt)
    {
        if (properties.containsKey(name))
        {
            return Integer.decode((String) properties.get(name));
        }
        else
        {
            return dflt;
        }
    }

    protected InboundEndpoint doBuildInboundEndpoint() throws InitialisationException, EndpointException
    {
        // use an explicit value here to avoid caching
        Map properties = getProperties();
        // this sets values used below, if they appear as properties
        setPropertiesFromProperties(properties);

        if (uriBuilder == null)
        {
            throw new MuleRuntimeException(CoreMessages.objectIsNull("uriBuilder"));
        }
        uriBuilder.setMuleContext(muleContext);
        EndpointURI endpointURI = uriBuilder.getEndpoint();
        endpointURI.initialise();

        Connector connector = getConnector();

        if (connector != null && endpointURI != null && !connector.supportsProtocol(endpointURI.getFullScheme()))
        {
            throw new IllegalArgumentException(CoreMessages.connectorSchemeIncompatibleWithEndpointScheme(
                    connector.getProtocol(), endpointURI).getMessage());
        }

        List transformers = getInboundTransformers(connector, endpointURI);
        List responseTransformers = getInboundEndpointResponseTransformers(connector, endpointURI);

        boolean synchronous = getSynchronous(connector, endpointURI);

        return new DefaultInboundEndpoint(connector, endpointURI, transformers, responseTransformers,
                getName(endpointURI), getProperties(), getTransactionConfig(), getFilter(connector),
                getDefaultDeleteUnacceptedMessages(connector), getSecurityFilter(), synchronous,
                getResponseTimeout(connector), getInitialState(connector), getEndpointEncoding(connector),
                name, muleContext, getRetryPolicyTemplate(connector));
    }

    protected OutboundEndpoint doBuildOutboundEndpoint() throws InitialisationException, EndpointException
    {
        // use an explicit value here to avoid caching
        Map properties = getProperties();
        // this sets values used below, if they appear as properties
        setPropertiesFromProperties(properties);

        if (uriBuilder == null)
        {
            throw new MuleRuntimeException(CoreMessages.objectIsNull("uriBuilder"));
        }
        uriBuilder.setMuleContext(muleContext);

        EndpointURI endpointURI = uriBuilder.getEndpoint();
        endpointURI.initialise();

        Connector connector = getConnector();

        if (connector != null && endpointURI != null && !connector.supportsProtocol(endpointURI.getFullScheme()))
        {
            throw new IllegalArgumentException(CoreMessages.connectorSchemeIncompatibleWithEndpointScheme(
                    connector.getProtocol(), endpointURI).getMessage());
        }

        List transformers = getOutboundTransformers(connector, endpointURI);
        List responseTransformers = getOutboundEndpointResponseTransformers(connector, endpointURI);

        boolean synchronous = getSynchronous(connector, endpointURI);

        return new DefaultOutboundEndpoint(connector, endpointURI, transformers, responseTransformers,
                getName(endpointURI), getProperties(), getTransactionConfig(), getFilter(connector),
                getDefaultDeleteUnacceptedMessages(connector), getSecurityFilter(), synchronous,
                getResponseTimeout(connector), getInitialState(connector), getEndpointEncoding(connector),
                name, muleContext, getRetryPolicyTemplate(connector), responsePropertiesList);
    }

    protected boolean getSynchronous(Connector connector, EndpointURI endpointURI)
    {
        return synchronous != null ? synchronous.booleanValue() : getDefaultSynchronous(connector,
                endpointURI.getScheme());
    }

    protected boolean getDefaultSynchronous(Connector connector, String protocol)
    {
        if (connector != null && connector.isSyncEnabled(protocol))
        {
            return true;
        }
        else
        {
            return muleContext.getConfiguration().isDefaultSynchronousEndpoints();
        }
    }

    protected RetryPolicyTemplate getRetryPolicyTemplate(Connector connector)
    {
        return retryPolicyTemplate != null ? retryPolicyTemplate : connector.getRetryPolicyTemplate();
    }

    protected TransactionConfig getTransactionConfig()
    {
        return transactionConfig != null ? transactionConfig : getDefaultTransactionConfig();
    }

    protected TransactionConfig getDefaultTransactionConfig()
    {
        // TODO Do we need a new instance per endpoint, or can a single instance be
        // shared?
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

    protected String getName(EndpointURI endpointURI)
    {
        return name != null ? name : new ObjectNameHelper(muleContext).getEndpointName(endpointURI);
    }

    protected Map getProperties()
    {
        // Add properties from builder, endpointURI and then seal (make unmodifiable)
        LinkedList maps = new LinkedList();
        // properties from url come first
        if (null != uriBuilder)
        {
            uriBuilder.setMuleContext(muleContext);
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
        return muleContext.getConfiguration().getDefaultEncoding();
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

    protected int getResponseTimeout(Connector connector)
    {
        return responseTimeout != null ? responseTimeout.intValue() : getDefaultResponseTimeout(connector);

    }

    protected int getDefaultResponseTimeout(Connector connector)
    {
        return muleContext.getConfiguration().getDefaultResponseTimeout();
    }

    protected List getInboundTransformers(Connector connector, EndpointURI endpointURI)
            throws TransportFactoryException
    {
        // #1 Transformers set on builder
        if (transformers != null)
        {
            return transformers;
        }

        // #2 Transformer specified on uri
        List transformers = getTransformersFromString(endpointURI.getTransformers());
        if (transformers != null)
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
            return TransformerUtils.getDefaultInboundTransformers(getNonNullServiceDescriptor(uriBuilder.getEndpoint()
                    .getSchemeMetaInfo(), getOverrides(connector)));
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
        if (transformers != null)
        {
            return transformers;
        }

        // #2 Transformer specified on uri
        List transformers = getTransformersFromString(endpointURI.getTransformers());
        if (transformers != null)
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
            return TransformerUtils.getDefaultOutboundTransformers(getNonNullServiceDescriptor(uriBuilder.getEndpoint()
                    .getSchemeMetaInfo(), getOverrides(connector)));
        }
        catch (Exception e)
        {
            throw new TransportFactoryException(e);
        }
    }

    protected List getInboundEndpointResponseTransformers(Connector connector, EndpointURI endpointURI)
            throws TransportFactoryException
    {
        // #1 Transformers set on builder
        if (responseTransformers != null)
        {
            return responseTransformers;
        }

        // #2 Transformer specified on uri
        List transformers = getTransformersFromString(endpointURI.getResponseTransformers());
        if (transformers != null)
        {
            return transformers;
        }

        // #3 Default Connector Response Transformer
        return getDefaultResponseTransformers(connector);
    }

    protected List getOutboundEndpointResponseTransformers(Connector connector, EndpointURI endpointURI)
            throws TransportFactoryException
    {
        // #1 Transformers set on builder
        if (responseTransformers != null)
        {
            return responseTransformers;
        }

        // #2 Transformer specified on uri
        List transformers = getTransformersFromString(endpointURI.getResponseTransformers());
        if (transformers != null)
        {
            return transformers;
        }
        return Collections.EMPTY_LIST;
    }

    protected List getDefaultResponseTransformers(Connector connector) throws TransportFactoryException
    {
        try
        {
            return TransformerUtils.getDefaultResponseTransformers(getNonNullServiceDescriptor(uriBuilder.getEndpoint()
                    .getSchemeMetaInfo(), getOverrides(connector)));
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
            return TransformerUtils.getTransformers(transformers, muleContext);
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
        TransportServiceDescriptor sd = (TransportServiceDescriptor) muleContext.getRegistry()
                .lookupServiceDescriptor(ServiceDescriptorFactory.TRANSPORT_SERVICE_TYPE, scheme, overrides);
        if (null != sd)
        {
            return sd;
        }
        else
        {
            throw new ServiceException(CoreMessages.noServiceTransportDescriptor(scheme));
        }
    }

    private Connector getConnector(EndpointURI endpointURI, MuleContext muleContext) throws EndpointException
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
                TransportFactory factory = new TransportFactory(muleContext);
                connector = factory.getConnectorByProtocol(scheme);
                if (connector == null)
                {
                    connector = factory.createConnector(endpointURI);
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
        if (transformers == null)
        {
            transformers = new LinkedList<Transformer>();
        }
        transformers.add(transformer);
    }

    public void setTransformers(List<Transformer> transformers)
    {               
        this.transformers = transformers;
    }

    public void addResponseTransformer(Transformer transformer)
    {
        if (responseTransformers == null)
        {
            responseTransformers = new LinkedList<Transformer>();
        }
        responseTransformers.add(transformer);
    }

    public void setResponseTransformers(List<Transformer> transformers)
    {
        this.responseTransformers = transformers;
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
     * @param key   the property key
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
        this.deleteUnacceptedMessages = Boolean.valueOf(deleteUnacceptedMessages);

    }

    public void setSecurityFilter(EndpointSecurityFilter securityFilter)
    {
        this.securityFilter = securityFilter;

    }

    public void setSynchronous(boolean synchronous)
    {
        this.synchronous = Boolean.valueOf(synchronous);

    }

    public void setResponseTimeout(int responseTimeout)
    {
        this.responseTimeout = new Integer(responseTimeout);

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

    public void setRetryPolicyTemplate(RetryPolicyTemplate retryPolicyTemplate)
    {
        this.retryPolicyTemplate = retryPolicyTemplate;

    }
    
    public URIBuilder getEndpointBuilder()
    {
        return uriBuilder;
    }

    public void setURIBuilder(URIBuilder URIBuilder)
    {
        this.uriBuilder = URIBuilder;

    }

    // TODO Equals() and hashCode() only needed for tests, move to tests
    public int hashCode()
    {
        return ClassUtils.hash(new Object[]{retryPolicyTemplate, connector, createConnector, deleteUnacceptedMessages,
                encoding, uriBuilder, filter, initialState, name, properties, responseTimeout,
                responseTransformers, securityFilter, synchronous, transactionConfig, transformers});
    }

    // TODO Equals() and hashCode() only needed for tests, move to tests
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        final AbstractEndpointBuilder other = (AbstractEndpointBuilder) obj;
        return equal(retryPolicyTemplate, other.retryPolicyTemplate) && equal(connector, other.connector)
                && equal(createConnector, other.createConnector)
                && equal(deleteUnacceptedMessages, other.deleteUnacceptedMessages) && equal(encoding, other.encoding)
                && equal(uriBuilder, other.uriBuilder) && equal(filter, other.filter)
                && equal(initialState, other.initialState) && equal(name, other.name)
                && equal(properties, other.properties)
                && equal(responseTimeout, other.responseTimeout)
                && equal(responseTransformers, other.responseTransformers)
                && equal(securityFilter, other.securityFilter) && equal(synchronous, other.synchronous)
                && equal(transactionConfig, other.transactionConfig) && equal(transformers, other.transformers);
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
        builder.setRetryPolicyTemplate(retryPolicyTemplate);

        if (deleteUnacceptedMessages != null)
        {
            builder.setDeleteUnacceptedMessages(deleteUnacceptedMessages.booleanValue());
        }
        if (synchronous != null)
        {
            builder.setSynchronous(synchronous.booleanValue());
        }

        if (responseTimeout != null)
        {
            builder.setResponseTimeout(responseTimeout.intValue());
        }

        return builder;
    }

}
