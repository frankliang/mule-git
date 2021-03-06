/*
 * $Header:
 * /cvsroot/mule/mule/src/test/org/mule/test/mule/AbstractMuleTestCase.java,v
 * 1.7 2003/11/24 09:58:47 rossmason Exp $ $Revision$ $Date: 2003/11/24
 * 09:58:47 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved. http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 *
 */
package org.mule.extras.spring.remoting;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;
import org.mule.umo.lifecycle.Initialisable;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.RecoverableException;
import org.mule.util.ClassHelper;
import org.mule.util.Utility;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocationResult;

public class SpringHttpInvokerComponent implements Initialisable, Callable
{
    private Delegate delegate;
    private Class serviceClass;
    private Class serviceInterface;
    private Object serviceBean;
    private boolean registerTraceInterceptor = false;
    private RemoteInvocationExecutor remoteInvocationExecutor;

    private class Delegate extends RemoteInvocationBasedExporter implements InitializingBean
    {
        private Object proxy;

        public void afterPropertiesSet()
        {
            this.proxy = getProxyForService();
        }

        public Object execute(RemoteInvocation invocation)
        {
            try {
                Object value = invoke(invocation, proxy);
                return value;
            }
            catch (Throwable ex) {
                ex.printStackTrace();
                return new RemoteInvocationResult(ex);
            }
        }
    }

    public SpringHttpInvokerComponent()
    {
        delegate = new Delegate();
    }

    public void initialise() throws InitialisationException, RecoverableException
    {
        if(serviceClass==null && serviceBean==null) {
            throw new InitialisationException(new Message(Messages.PROPERTIES_X_NOT_SET, "serviceClass or serviceBean"), this);
        }
        if(serviceInterface==null) {
            throw new InitialisationException(new Message(Messages.PROPERTIES_X_NOT_SET, "serviceInterface"), this);
        }

        if(serviceClass!=null && !serviceClass.equals(Utility.EMPTY_STRING))
        {
            Object service = null;
            try {
                service = ClassHelper.instanciateClass(serviceClass, null);
            } catch (Exception e) {
                throw new InitialisationException(e, this);
            }
            delegate.setService(service);
        } else if(serviceBean!=null)
        {
            delegate.setService(serviceBean);
        }
        delegate.setServiceInterface(serviceInterface);
        delegate.setRegisterTraceInterceptor(registerTraceInterceptor);
        if(remoteInvocationExecutor!=null) {
            delegate.setRemoteInvocationExecutor(remoteInvocationExecutor);
        }
        delegate.afterPropertiesSet();
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Object getServiceBean() {
        return serviceBean;
    }

    public void setServiceBean(Object serviceBean) {
        this.serviceBean = serviceBean;
    }

    public Class getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public boolean isRegisterTraceInterceptor() {
        return registerTraceInterceptor;
    }

    public void setRegisterTraceInterceptor(boolean registerTraceInterceptor) {
        this.registerTraceInterceptor = registerTraceInterceptor;
    }

    public RemoteInvocationExecutor getRemoteInvocationExecutor() {
        return remoteInvocationExecutor;
    }

    public void setRemoteInvocationExecutor(RemoteInvocationExecutor remoteInvocationExecutor) {
        this.remoteInvocationExecutor = remoteInvocationExecutor;
    }

    public Object onCall(UMOEventContext eventContext) throws Exception
    {
        Object transformedMessage = eventContext.getTransformedMessage();
        RemoteInvocation ri = (RemoteInvocation) transformedMessage;
        Object rval = delegate.execute(ri);
        return rval;
    }
}
