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
 */
package org.mule.providers.servlet;

import org.mule.tck.providers.AbstractMessageAdapterTestCase;
import org.mule.umo.provider.UMOMessageAdapter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class HttpRequestMessageAdapterTestCase extends AbstractMessageAdapterTestCase
{
    public Object getValidMessage() throws Exception
    {
        return getMockRequest("test message");
    }

    public UMOMessageAdapter createAdapter(Object payload) throws Exception
    {
        return new HttpRequestMessageAdapter(payload);
    }

    public static HttpServletRequest getMockRequest(final String message)
    {
        Object proxy = Proxy.newProxyInstance(ServletConnectorTestCase.class.getClassLoader(), new Class[]{HttpServletRequest.class}, new InvocationHandler() {
            private String payload = message;
            private Map props = new HashMap();
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                if("getInputStream".equals(method.getName())) {

                    ServletInputStream s = new ServletInputStream() {
                        ByteArrayInputStream is = new ByteArrayInputStream(payload.getBytes());
                        public int read() throws IOException
                        {
                            return is.read();
                        }
                    };
                    return s;

                }
                else if("getAttribute".equals(method.getName())) {
                    return props.get(args[0]);
                } else if("setAttribute".equals(method.getName())) {
                    props.put(args[0], args[1]);
                } else if("equals".equals(method.getName())) {
                    return new Boolean(payload.equals(args[0]));
                } else if("toString".equals(method.getName())) {
                    return payload;
                }
                return null;
            }
        });
        return (HttpServletRequest)proxy;
    }
}
