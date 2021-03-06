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
package org.mule.impl.container;

import java.io.Reader;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.manager.ContainerException;
import org.mule.umo.manager.ObjectNotFoundException;

/**
 * <code>JndiContainerContext</code> is a container implementaiton that
 * exposes a jndi context. What ever properties are set on the container in
 * configuration will be passed to the initial context.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class JndiContainerContext extends AbstractContainerContext
{
    protected InitialContext context;
    private Map environment;

    public JndiContainerContext()
    {
        super("jndi");
    }

    protected JndiContainerContext(String name)
    {
        super(name);
    }

    public Map getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(Map environment)
    {
        this.environment = environment;
    }

    public InitialContext getContext() {
        return context;
    }

    public void setContext(InitialContext context) {
        this.context = context;
    }

    public Object getComponent(Object key) throws ObjectNotFoundException
    {
        try {
            if(key==null) {
                throw new ObjectNotFoundException("null");
            }
            if (key instanceof Name) {
                return context.lookup((Name) key);
            } else if(key instanceof Class){
                return context.lookup(((Class)key).getName());
            } else {
                return context.lookup(key.toString());
            }
        } catch (NamingException e) {
            throw new ObjectNotFoundException(key.toString(), e);
        }
    }

    public void configure(Reader configuration) throws ContainerException
    {
        throw new UnsupportedOperationException("configure(Reader)");
    }

    public void initialise() throws InitialisationException
    {
        try {
            if(context==null) {
                context = new InitialContext(new Hashtable(environment));
            }
        } catch (NamingException e) {
            throw new InitialisationException(new Message(Messages.FAILED_TO_CREATE_X, "Jndi context"), e, this);
        }
    }
}
