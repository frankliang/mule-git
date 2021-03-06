/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.http.jetty;

import org.mule.MuleServer;
import org.mule.RegistryContext;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.config.ThreadingProfile;
import org.mule.api.endpoint.Endpoint;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.lifecycle.LifecycleException;
import org.mule.api.service.Service;
import org.mule.api.transport.Connector;
import org.mule.config.i18n.CoreMessages;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.http.i18n.HttpMessages;
import org.mule.transport.http.servlet.MuleRESTReceiverServlet;
import org.mule.transport.http.servlet.ServletConnector;
import org.mule.util.StringUtils;

import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.util.InetAddrPort;

/**
 * <code>HttpMessageReceiver</code> is a simple http server that can be used to
 * listen for http requests on a particular port
 */
public class JettyHttpMessageReceiver extends AbstractMessageReceiver
{
    public static final String JETTY_SERVLET_CONNECTOR_NAME = "_jettyConnector";

    private Server httpServer;

    public JettyHttpMessageReceiver(Connector connector, Service service, Endpoint endpoint)
            throws CreateException
    {

        super(connector, service, endpoint);

        if ("rest".equals(endpoint.getEndpointURI().getScheme()))
        {
            // We need tohave  a Servlet Connector pointing to our servlet so the Servlets can
            // find the listeners for incoming requests
            ServletConnector scon = (ServletConnector) RegistryContext.getRegistry().lookupConnector(JETTY_SERVLET_CONNECTOR_NAME);
            if (scon != null)
            {
                throw new CreateException(
                        HttpMessages.noServletConnectorFound(JETTY_SERVLET_CONNECTOR_NAME), this);
            }

            scon = new ServletConnector();
            scon.setName(JETTY_SERVLET_CONNECTOR_NAME);
            scon.setServletUrl(endpoint.getEndpointURI().getAddress());
            try
            {
                MuleContext muleContext = MuleServer.getMuleContext();
                scon.setMuleContext(muleContext);
                //muleContext.applyLifecycle(scon);
                muleContext.getRegistry().registerConnector(scon);

                String path = endpoint.getEndpointURI().getPath();
                if (StringUtils.isEmpty(path))
                {
                    path = "/";
                }

                EndpointBuilder endpointBuilder = new EndpointURIEndpointBuilder("servlet://" + path.substring(1),
                    connector.getMuleContext());
                endpointBuilder.setTransformers(endpoint.getTransformers());
                ImmutableEndpoint ep = connector.getMuleContext()
                    .getRegistry()
                    .lookupEndpointFactory()
                    .getInboundEndpoint(endpointBuilder);
                scon.registerListener(service, ep);
            }
            catch (Exception e)
            {
                throw new CreateException(e, this);
            }
        }

    }

    protected void doConnect() throws Exception
    {
        httpServer = new Server();
        SocketListener socketListener = new SocketListener(new InetAddrPort(endpoint.getEndpointURI()
                .getPort()));

        // apply Threading settings
        ThreadingProfile tp = connector.getReceiverThreadingProfile();
        socketListener.setMaxIdleTimeMs((int) tp.getThreadTTL());
        int threadsActive = tp.getMaxThreadsActive();
        int threadsMin = socketListener.getMinThreads();
        if (threadsMin >= threadsActive)
        {
            socketListener.setMinThreads(threadsActive - 1);
        }
        socketListener.setMaxThreads(threadsActive);
        // thread priorities are evil and gone from ThreadingProfile
        // (google for priority inversion)
        // socketListener.setThreadsPriority(tp.getThreadPriority());

        httpServer.addListener(socketListener);

        String path = endpoint.getEndpointURI().getPath();
        if (StringUtils.isEmpty(path))
        {
            path = "/";
        }

        if (!path.endsWith("/"))
        {
            path += "/";
        }

        HttpContext context = httpServer.getContext("/");
        context.setRequestLog(null);

        ServletHandler handler = new ServletHandler();
        if ("rest".equals(endpoint.getEndpointURI().getScheme()))
        {
            handler.addServlet("MuleRESTReceiverServlet", path + "*", MuleRESTReceiverServlet.class.getName());
        }
        else
        {
            handler.addServlet("JettyReceiverServlet", path + "*", JettyReceiverServlet.class.getName());
        }


        context.addHandler(handler);
        // setAttribute is already synchronized in Jetty
        context.setAttribute("messageReceiver", this);

    }

    protected void doDisconnect() throws Exception
    {
        // stop is automativcally called by Mule
    }


    /**
     * Template method to dispose any resources associated with this receiver. There
     * is not need to dispose the connector as this is already done by the framework
     */
    protected void doDispose()
    {
        try
        {
            httpServer.stop(false);
        }
        catch (InterruptedException e)
        {
            logger.error("Error disposing Jetty recevier on: " + endpoint.getEndpointURI().toString(), e);
        }
    }

    protected void doStart() throws MuleException
    {
        try
        {
            httpServer.start();
        }
        catch (Exception e)
        {
            throw new LifecycleException(CoreMessages.failedToStart("Jetty Http Receiver"), e, this);
        }
    }

    protected void doStop() throws MuleException
    {
        try
        {
            httpServer.stop(true);
        }
        catch (InterruptedException e)
        {
            throw new LifecycleException(CoreMessages.failedToStop("Jetty Http Receiver"), e, this);
        }
    }

}
