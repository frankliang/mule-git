/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers;

import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.service.ConnectorFactory;
import org.mule.providers.service.ConnectorServiceDescriptor;
import org.mule.providers.service.ConnectorServiceException;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.umo.provider.UMOStreamMessageAdapter;
import org.mule.util.BeanUtils;
import org.mule.util.ObjectNameHelper;
import org.mule.util.PropertiesUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <code>AbstractServiceEnabledConnector</code> initialises a connector from a
 * service descriptor. Using this method greatly reduces the code required to
 * implement a connector and means that Mule can create connectors and endpoints from
 * a url if the connector has a service descriptor.
 * 
 * @see ConnectorServiceDescriptor
 */

public abstract class AbstractServiceEnabledConnector extends AbstractConnector
{
    /**
     * Holds the service configuration for this connector
     */
    protected ConnectorServiceDescriptor serviceDescriptor;

    protected Properties serviceOverrides;

    public void doInitialise() throws InitialisationException
    {
        initFromServiceDescriptor();
    }

    public void initialiseFromUrl(UMOEndpointURI endpointUri) throws InitialisationException
    {
        if (!supportsProtocol(endpointUri.getFullScheme()))
        {
            throw new InitialisationException(new Message(Messages.SCHEME_X_NOT_COMPATIBLE_WITH_CONNECTOR_X,
                endpointUri.getFullScheme(), getClass().getName()), this);
        }
        Properties props = new Properties();
        props.putAll(endpointUri.getParams());
        // auto set username and password
        if (endpointUri.getUserInfo() != null)
        {
            props.setProperty("username", endpointUri.getUsername());
            String passwd = endpointUri.getPassword();
            if (passwd != null)
            {
                props.setProperty("password", passwd);
            }
        }
        String host = endpointUri.getHost();
        if (host != null)
        {
            props.setProperty("hostname", host);
            props.setProperty("host", host);
        }
        if (endpointUri.getPort() > -1)
        {
            props.setProperty("port", String.valueOf(endpointUri.getPort()));
        }

        BeanUtils.populateWithoutFail(this, props, true);

        setName(ObjectNameHelper.getConnectorName(this));
    }

    protected synchronized void initFromServiceDescriptor() throws InitialisationException
    {
        try
        {
            serviceDescriptor = ConnectorFactory.getServiceDescriptor(getProtocol().toLowerCase(),
                serviceOverrides);

            if (serviceDescriptor.getDispatcherFactory() != null)
            {
                logger.debug("Loading DispatcherFactory: " + serviceDescriptor.getDispatcherFactory());
                this.setDispatcherFactory(serviceDescriptor.createDispatcherFactory());
            }

            defaultInboundTransformer = serviceDescriptor.createInboundTransformer();
            defaultOutboundTransformer = serviceDescriptor.createOutboundTransformer();
            defaultResponseTransformer = serviceDescriptor.createResponseTransformer();

            sessionHandler = serviceDescriptor.createSessionHandler();

            // Set any manager default properties for the connector. These are set on
            // the Manager with a protocol e.g. jms.specification=1.1
            // This provides a really convenient way to set properties on an object
            // from unit tests
            Map props = new HashMap();
            PropertiesUtils.getPropertiesWithPrefix(MuleManager.getInstance().getProperties(),
                getProtocol().toLowerCase(), props);
            if (props.size() > 0)
            {
                props = PropertiesUtils.removeNamespaces(props);
                org.mule.util.BeanUtils.populateWithoutFail(this, props, true);
            }
        }
        catch (Exception e)
        {
            throw new InitialisationException(e, this);
        }
    }

    protected ConnectorServiceDescriptor getServiceDescriptor()
    {
        if (serviceDescriptor == null)
        {
            throw new IllegalStateException("This connector has not yet been initialised: " + name);
        }
        return serviceDescriptor;
    }

    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception
    {
        return getServiceDescriptor().createMessageReceiver(this, component, endpoint);
    }

    /**
     * Gets a <code>UMOMessageAdapter</code> for the endpoint for the given message
     * (data)
     * 
     * @param message the data with which to initialise the
     *            <code>UMOMessageAdapter</code>
     * @return the <code>UMOMessageAdapter</code> for the endpoint
     * @throws org.mule.umo.MessagingException if the message parameter is not
     *             supported
     * @see org.mule.umo.provider.UMOMessageAdapter
     */
    public UMOMessageAdapter getMessageAdapter(Object message) throws MessagingException
    {
        try
        {
            return serviceDescriptor.createMessageAdapter(message);
        }
        catch (ConnectorServiceException e)
        {
            throw new MessagingException(new Message(Messages.FAILED_TO_CREATE_X, "Message Adapter"),
                message, e);
        }
    }

    public UMOStreamMessageAdapter getStreamMessageAdapter(InputStream in, OutputStream out)
        throws MessagingException
    {
        try
        {
            return serviceDescriptor.createStreamMessageAdapter(in, out);
        }
        catch (ConnectorServiceException e)
        {
            throw new MessagingException(new Message(Messages.FAILED_TO_CREATE_X, "Stream Message Adapter"),
                in, e);
        }
    }

    public Map getServiceOverrides()
    {
        return serviceOverrides;
    }

    public void setServiceOverrides(Map serviceOverrides)
    {
        this.serviceOverrides = new Properties();
        this.serviceOverrides.putAll(serviceOverrides);
    }
}
