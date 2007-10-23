/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.bootstrap;

import org.mule.impl.ManagementContextAware;
import org.mule.registry.Registry;
import org.mule.umo.UMOException;
import org.mule.umo.UMOManagementContext;
import org.mule.umo.lifecycle.Initialisable;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.UMOTransformer;
import org.mule.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * This object will load objects defined in a file called <code>registry-bootstrap.properties</code> into the local registry.
 * This allows modules and transports to make certain objects available by default.  The most common use case is for a
 * module or transport to load stateless transformers into the registry.
 * For this file to be located it must be present in the modules META-INF directory under
 * <code>META-INF/services/org/mule/config/</code>
 *
 * The format of this file is a simple key / value pair. i.e.
 * <code>
 * myobject=org.foo.MyObject
 * </code>
 * 
 * Will register an instance of MyObject with a key of 'myobject'. If you don't care about the object name and want to 
 * ensure that the ojbect gets a unique name you can use -
 * <code>
 * object.1=org.foo.MyObject
 * object.2=org.bar.MyObject
 * </code>
 *
 * Loading transformers has a slightly different notation since you can define the 'returnClass' of the transformer.
 * To define a transformer with a return type simply add the return type class in brackets at the end of the transformer class i.e.
 *
 * <code>
 * transformer.1=org.mule.providers.jms.transformers.JMSMessageToObject(byte[])
 * transformer.2=org.mule.providers.jms.transformers.JMSMessageToObject(java.lang.String)
 * transformer.3=org.mule.providers.jms.transformers.JMSMessageToObject(java.util.Hashtable)
 * transformer.4=org.mule.providers.jms.transformers.JMSMessageToObject(java.util.Vector)
 * transformer.5=org.mule.providers.jms.transformers.JMSMessageToObject(java.lang.Object)
 * ransformer.6=org.mule.providers.jms.transformers.ObjectToJMSMessage(javax.jms.Message)
 * </code>
 *
 * Note that the key used for transformers must be 'transformer.x' where 'x' is a sequential number.  The transformer name will be
 * automatically generated as JMSMessageToXXX where XXX is the return class name i.e. JMSMessageToString
 *
 * Note that all objects defined have to have a default constructor. They can implement injection interfaces such as
 * {@link org.mule.impl.ManagementContextAware} and lifecylce interfaces such as {@link org.mule.umo.lifecycle.Initialisable}.
 */
public class SimpleRegistryBootstrap implements Initialisable, ManagementContextAware
{
    public static final String SERVICE_PATH = "META-INF/services/org/mule/config/";

    public static final String REGISTRY_PROPERTIES = "registry-bootstrap.properties";

    public String TRANSFORMER_PREFIX = "transformer.";
    public String OBJECT_PREFIX = "object.";


    protected UMOManagementContext context;

    /** {@inheritDoc} */
    public void setManagementContext(UMOManagementContext context)
    {
        this.context = context;
    }

    /** {@inheritDoc} */
    public void initialise() throws InitialisationException
    {
        Enumeration e = ClassUtils.getResources(SERVICE_PATH + REGISTRY_PROPERTIES, getClass());
        while(e.hasMoreElements())
        {
            try
            {
                URL url = (URL)e.nextElement();
                Properties p = new Properties();
                p.load(url.openStream());
                process(p);
            }
            catch (Exception e1)
            {
                throw new InitialisationException(e1, this);
            }
        }
    }

    protected void process(Properties props) throws NoSuchMethodException, IllegalAccessException, UMOException, InvocationTargetException, ClassNotFoundException, InstantiationException
    {
        registerTransformers(props, context.getRegistry());
        registerUnnamedObjects(props, context.getRegistry());
        //this must be called last as it clears the properties map
        registerObjects(props, context.getRegistry());

    }

    private void registerTransformers(Properties props, Registry registry) throws UMOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException
    {
        int i = 1;
        String transString = props.getProperty(TRANSFORMER_PREFIX + i);
        while (transString != null)
        {
            Class returnClass = null;

            int pos = transString.indexOf('(');
            if (pos == -1)
            {
                pos = transString.length();
            }
            String clazz = transString.substring(0, pos);
            if (pos != transString.length())
            {
                String returnClassString = transString.substring(pos + 1, transString.length() - 1);
                if (returnClassString.equals("byte[]"))
                {
                    returnClass = byte[].class;
                }
                else
                {
                    returnClass = ClassUtils.loadClass(returnClassString, getClass());
                }
            }
            UMOTransformer trans = (UMOTransformer) ClassUtils.instanciateClass(clazz, ClassUtils.NO_ARGS);
            if(returnClass!=null)
            {
                trans.setReturnClass(returnClass);
            }
            registry.registerTransformer(trans);
            props.remove(TRANSFORMER_PREFIX + i++);
            transString = props.getProperty(TRANSFORMER_PREFIX + i);
        }
    }

    private void registerObjects(Properties props, Registry registry) throws UMOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException
    {
        //Note that caling the other register methods first will have removed any processed entries
        for (Iterator iterator = props.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object object = ClassUtils.instanciateClass(entry.getValue().toString(), ClassUtils.NO_ARGS);
            String key = entry.getKey().toString();
            registry.registerObject(key, object);
        }
        props.clear();
    }

    private void registerUnnamedObjects(Properties props, Registry registry) throws UMOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException
    {
        int i = 1;
        String objectString = props.getProperty(OBJECT_PREFIX + i);
        while (objectString != null)
        {

            Object o = ClassUtils.instanciateClass(objectString, ClassUtils.NO_ARGS);
            registry.registerObject(OBJECT_PREFIX + i + "#" + o.hashCode(), o);
            props.remove(OBJECT_PREFIX + i++);
            objectString = props.getProperty(OBJECT_PREFIX + i);
        }
    }
}
