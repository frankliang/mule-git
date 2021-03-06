/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.components.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.util.properties.PropertyExtractor;
import org.mule.util.properties.MessagePropertyExtractor;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.NullPayload;
import org.mule.routing.filters.MessagePropertyFilter;
import org.mule.routing.filters.RegExFilter;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.Callable;
import org.mule.umo.lifecycle.Initialisable;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.RecoverableException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This component can used to proxy REST style services as local Mule Components. It
 * can be configured with a service URL plus a number of properties that allow you to
 * configure the parameters and error conditions on the service.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class RestServiceWrapper implements Callable, Initialisable
{
    public static final String REST_SERVICE_URL = "rest.service.url";

    /**
     * logger used by this class
     */
    protected transient Log logger = LogFactory.getLog(getClass());

    private String serviceUrl;
    private boolean urlFromMessage = false;
    private Map reqiredParams = new HashMap();
    private Map optionalParams = new HashMap();
    private String httpMethod = "GET";
    private String payloadParameterName;
    private UMOFilter errorFilter;
    private String errorExpression;

    private PropertyExtractor propertyExtractor = new MessagePropertyExtractor();

    public String getServiceUrl()
    {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl)
    {
        this.serviceUrl = serviceUrl;
    }

    public boolean isUrlFromMessage()
    {
        return urlFromMessage;
    }

    public void setUrlFromMessage(boolean urlFromMessage)
    {
        this.urlFromMessage = urlFromMessage;
    }

    public Map getReqiredParams()
    {
        return reqiredParams;
    }

    public void setReqiredParams(Map reqiredParams)
    {
        this.reqiredParams = reqiredParams;
    }

    public Map getOptionalParams()
    {
        return optionalParams;
    }

    public void setOptionalParams(Map optionalParams)
    {
        this.optionalParams = optionalParams;
    }

    public String getHttpMethod()
    {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public String getPayloadParameterName()
    {
        return payloadParameterName;
    }

    public void setPayloadParameterName(String payloadParameterName)
    {
        this.payloadParameterName = payloadParameterName;
    }

    public UMOFilter getErrorFilter()
    {
        return errorFilter;
    }

    public void setErrorFilter(UMOFilter errorFilter)
    {
        this.errorFilter = errorFilter;
    }

    public String getErrorExpression()
    {
        return errorExpression;
    }

    public void setErrorExpression(String errorExpression)
    {
        this.errorExpression = errorExpression;
    }

    public void initialise() throws InitialisationException, RecoverableException
    {
        if (serviceUrl == null && !urlFromMessage)
        {
            throw new InitialisationException(new Message(Messages.X_IS_NULL, "serviceUrl"), this);
        }
        else if (serviceUrl != null)
        {
            try
            {
                new URL(serviceUrl);
            }
            catch (MalformedURLException e)
            {
                throw new InitialisationException(e, this);
            }
        }

        if (errorFilter == null)
        {
            if (errorExpression == null)
            {
                // We'll set a default filter that checks the return code
                errorFilter = new MessagePropertyFilter("http.status!=200");
                logger.info("Setting default error filter to MessagePropertyFilter('http.status!=200')");
            }
            else
            {
                errorFilter = new RegExFilter(errorExpression);
            }
        }
    }

    public Object onCall(UMOEventContext eventContext) throws Exception
    {
        String tempUrl;
        Object request = eventContext.getTransformedMessage();
        Object requestBody = request;
        if (urlFromMessage)
        {
            tempUrl = eventContext.getMessage().getStringProperty(REST_SERVICE_URL, null);
            if (tempUrl == null)
            {
                throw new IllegalArgumentException(new Message(Messages.X_PROPERTY_IS_NOT_SET_ON_EVENT,
                    REST_SERVICE_URL).toString());
            }
        }
        else
        {
            tempUrl = serviceUrl;
        }
        StringBuffer urlBuffer = new StringBuffer(tempUrl);

        if (payloadParameterName != null)
        {
            requestBody = new NullPayload();
        }
        else if (request instanceof Map)
        {
            requestBody = new NullPayload();
        }

        setRESTParams(urlBuffer, eventContext.getMessage(), request, reqiredParams, false);
        setRESTParams(urlBuffer, eventContext.getMessage(), request, optionalParams, true);

        tempUrl = urlBuffer.toString();
        logger.info("Invoking REST service: " + tempUrl);

        UMOEndpointURI endpointURI = new MuleEndpointURI(tempUrl);
        eventContext.getMessage().setProperty("http.method", httpMethod);

        UMOMessage result = eventContext.sendEvent(new MuleMessage(requestBody, eventContext.getMessage()),
            endpointURI);

        if (isErrorPayload(result))
        {
            handleException(new RestServiceException(new Message(Messages.FAILED_TO_INVOKE_REST_SERVICE_X,
                tempUrl), result), result);
        }
        return result;
    }

    private void setRESTParams(StringBuffer url, UMOMessage msg, Object body, Map args, boolean optional)
    {
        char sep;

        if (url.indexOf("?") > -1)
        {
            sep = '&';
        }
        else
        {
            sep = '?';
        }

        for (Iterator iterator = args.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry)iterator.next();
            String name = (String)entry.getKey();
            String exp = (String)entry.getValue();
            Object value = propertyExtractor.getProperty(exp, msg);

            if (value == null && !optional)
            {
                throw new IllegalArgumentException(
                    new Message(Messages.X_PROPERTY_IS_NOT_SET_ON_EVENT, exp).toString());
            }

            url.append(sep);
            sep = '&';
            url.append(name).append('=').append(value);
        }

        if (!optional && payloadParameterName != null)
        {
            url.append(sep).append(payloadParameterName).append('=').append(body.toString());
        }
    }

    protected boolean isErrorPayload(UMOMessage message)
    {
        if (errorFilter != null)
        {
            return errorFilter.accept(message);
        }
        return false;
    }

    protected void handleException(RestServiceException e, UMOMessage result) throws Exception
    {
        throw e;
    }
}
