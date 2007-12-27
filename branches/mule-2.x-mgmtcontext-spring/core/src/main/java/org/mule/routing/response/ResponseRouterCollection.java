/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.routing.response;

import org.mule.config.MuleConfiguration;
import org.mule.config.i18n.CoreMessages;
import org.mule.management.stats.RouterStatistics;
import org.mule.routing.AbstractRouterCollection;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.InvalidEndpointTypeException;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.routing.RoutingException;
import org.mule.umo.routing.UMOResponseRouter;
import org.mule.umo.routing.UMOResponseRouterCollection;
import org.mule.umo.routing.UMORouter;

import java.util.Iterator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;

/**
 * <code>ResponseRouterCollection</code> is a router that can be used to control how
 * the response in a request/response message flow is created. Main usecase is to
 * aggregate a set of asynchonous events into a single response
 */
public class ResponseRouterCollection extends AbstractRouterCollection implements UMOResponseRouterCollection
{
    private volatile List endpoints = new CopyOnWriteArrayList();
    private volatile int timeout = MuleConfiguration.DEFAULT_TIMEOUT;
    private volatile boolean failOnTimeout = true;

    public ResponseRouterCollection()
    {
        super(RouterStatistics.TYPE_RESPONSE);
    }


    public void initialise() throws InitialisationException
    {
        super.initialise();
        for (Iterator iterator = endpoints.iterator(); iterator.hasNext();)
        {
            UMOImmutableEndpoint endpoint = (UMOImmutableEndpoint) iterator.next();
            endpoint.initialise();
        }
    }

    public void route(UMOEvent event) throws RoutingException
    {
        UMOResponseRouter router;
        for (Iterator iterator = getRouters().iterator(); iterator.hasNext();)
        {
            router = (UMOResponseRouter) iterator.next();
            router.process(event);
            // Update stats
            if (getStatistics().isEnabled())
            {
                getStatistics().incrementRoutedMessage(event.getEndpoint());
            }
        }
    }

    public UMOMessage getResponse(UMOMessage message) throws RoutingException
    {
        UMOMessage result = null;
        if (routers.size() == 0)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("There are no routers configured on the response router. Returning the current message");
            }
            result = message;
        }
        else
        {
            UMOResponseRouter router;
            for (Iterator iterator = getRouters().iterator(); iterator.hasNext();)
            {
                router = (UMOResponseRouter) iterator.next();
                result = router.getResponse(message);
            }

            if (result == null)
            {
                // Update stats
                if (getStatistics().isEnabled())
                {
                    getStatistics().incrementNoRoutedMessage();
                }
            }
        }

        // if (result != null && transformer != null) {
        // try {
        // result = new MuleMessage(transformer.transform(result.getPayload()),
        // result.getProperties());
        // } catch (TransformerException e) {
        // throw new RoutingException(result, null);
        // }
        // }
        return result;

    }

    public void addRouter(UMORouter router)
    {
        ((UMOResponseRouter) router).setTimeout(getTimeout());
        ((UMOResponseRouter) router).setFailOnTimeout(isFailOnTimeout());
        routers.add(router);
    }

    public UMOResponseRouter removeRouter(UMOResponseRouter router)
    {
        if (routers.remove(router))
        {
            return router;
        }
        else
        {
            return null;
        }
    }

    public void addEndpoint(UMOImmutableEndpoint endpoint)
    {
        if (endpoint != null)
        {
            if (!UMOImmutableEndpoint.ENDPOINT_TYPE_RESPONSE.equals(endpoint.getType()))
            {
                throw new InvalidEndpointTypeException(CoreMessages.responseRouterMustUseResponseEndpoints(
                    this, endpoint));
            }
            endpoints.add(endpoint);
        }
        else
        {
            throw new IllegalArgumentException("endpoint = null");
        }
    }

    public boolean removeEndpoint(UMOImmutableEndpoint endpoint)
    {
        return endpoints.remove(endpoint);
    }

    public List getEndpoints()
    {
        return endpoints;
    }

    public void setEndpoints(List endpoints)
    {
        if (endpoints != null)
        {
            this.endpoints.clear();
            this.endpoints.addAll(endpoints);

            // Ensure all endpoints are response endpoints
            for (Iterator it = this.endpoints.iterator(); it.hasNext();)
            {
                UMOImmutableEndpoint endpoint=(UMOImmutableEndpoint) it.next();
                if (!UMOImmutableEndpoint.ENDPOINT_TYPE_RESPONSE.equals(endpoint.getType()))
                {
                    throw new InvalidEndpointTypeException(CoreMessages.responseRouterMustUseResponseEndpoints(
                        this, endpoint));
                }
            }

        }
        else
        {
            throw new IllegalArgumentException("List of endpoints = null");
        }
    }

    /**
     * @param name the Endpoint identifier
     * @return the Endpoint or null if the endpointUri is not registered
     * @see org.mule.umo.routing.UMOInboundRouterCollection
     */
    public UMOImmutableEndpoint getEndpoint(String name)
    {
        UMOImmutableEndpoint endpointDescriptor;
        for (Iterator iterator = endpoints.iterator(); iterator.hasNext();)
        {
            endpointDescriptor = (UMOImmutableEndpoint) iterator.next();
            if (endpointDescriptor.getName().equals(name))
            {
                return endpointDescriptor;
            }
        }
        return null;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }


    public boolean isFailOnTimeout()
    {
        return failOnTimeout;
    }

    public void setFailOnTimeout(boolean failOnTimeout)
    {
        this.failOnTimeout = failOnTimeout;
    }


    public boolean hasEndpoints()
    {
        return !getEndpoints().isEmpty();
    }
}
