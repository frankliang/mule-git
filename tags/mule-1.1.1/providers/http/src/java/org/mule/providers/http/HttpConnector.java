/* 

 * $Header$

 * $Revision$

 * $Date$

 * ------------------------------------------------------------------------------------------------------

 * 

 * Copyright (c) SymphonySoft Limited. All rights reserved.

 * http://www.symphonysoft.com

 * 

 * The software in this package is published under the terms of the BSD

 * style license a copy of which has been included with this distribution in

 * the LICENSE.txt file. 

 *

 */

package org.mule.providers.http;

import org.mule.providers.tcp.TcpConnector;

import java.util.Map;

/**
 * <code>HttpConnector</code> provides a way of receiving and sending http
 * requests and responses. The UMOConnector itself handles dispatching http
 * requests. The <code>HttpMessageReceiver</code> handles the receiving
 * requests and processing of headers This endpoint recognises the following
 * properties - <p/>
 * <ul>
 * <li>hostname - The hostname to send and receive http requests</li>
 * <li>port - The port to listen on. The industry standard is 80 and if this
 * propert is not set it will default to 80</li>
 * <li>proxyHostname - If you access the web through a proxy, this holds the
 * server address</li>
 * <li>proxyPort - The port the proxy is configured on</li>
 * <li>proxyUsername - If the proxy requires authentication supply a username</li>
 * <li>proxyPassword - If the proxy requires authentication supply a password</li>
 * </ul>
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */

public class HttpConnector extends TcpConnector
{

    /**
     * Event property to pass back the status for the response
     */
    public static final String HTTP_STATUS_PROPERTY = "http.status";
    public static final String HTTP_VERSION_PROPERTY = "http.version";
    public static final String HTTP_CUSTOM_HEADERS_MAP_PROPERTY = "http.custom.headers";
    public static final String HTTP_METHOD_PROPERTY = "http.method";
    public static final String HTTP_REQUEST_PROPERTY = "http.request";
    public static final String HTTP_PARAMS = "http.params";
    public static final String HTTP_GET_BODY_PARAM_PROPERTY = "http.get.body.param";
    public static final String DEFAULT_HTTP_GET_BODY_PARAM_PROPERTY = "body";
    public static final String HTTP_POST_BODY_PARAM_PROPERTY = "http.post.body.param";

    public static final int DEFAULT_PORT = 80;
    public static String DEFAULT_HTTP_VERSION = HttpConstants.HTTP11;

    private String proxyHostname = null;

    private int proxyPort = DEFAULT_PORT;

    private String proxyUsername = null;

    private String proxyPassword = null;

    private long keepAliveTimeout = 120000;

    private boolean keepAlive = true;

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#getProtocol()
     */
    public String getProtocol()
    {
        return "http";
    }

    /**
     * @return
     */
    public String getProxyHostname()
    {
        return proxyHostname;
    }

    /**
     * @return
     */
    public String getProxyPassword()
    {
        return proxyPassword;
    }

    /**
     * @return
     */
    public int getProxyPort()
    {
        return proxyPort;
    }

    /**
     * @return
     */
    public String getProxyUsername()
    {
        return proxyUsername;
    }

    /**
     * @param host
     */
    public void setProxyHostname(String host)
    {
        proxyHostname = host;
    }

    /**
     * @param string
     */
    public void setProxyPassword(String string)
    {
        proxyPassword = string;
    }

    /**
     * @param port
     */
    public void setProxyPort(int port)
    {
        proxyPort = port;
    }

    /**
     * @param string
     */
    public void setProxyUsername(String string)
    {
        proxyUsername = string;
    }

    public long getKeepAliveTimeout()
    {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(long keepAliveTimeout)
    {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public boolean isKeepAlive()
    {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive)
    {
        this.keepAlive = keepAlive;
    }

    public Map getReceivers() {
        return this.receivers;
    }
}
