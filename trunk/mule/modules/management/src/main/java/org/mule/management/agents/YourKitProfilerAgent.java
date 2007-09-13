/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.management.agents;

import org.mule.config.i18n.CoreMessages;
import org.mule.management.i18n.ManagementMessages;
import org.mule.management.mbeans.YourKitProfilerService;
import org.mule.management.support.AutoDiscoveryJmxSupportFactory;
import org.mule.management.support.JmxSupport;
import org.mule.management.support.JmxSupportFactory;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.manager.UMOAgent;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class YourKitProfilerAgent implements UMOAgent
{

    private String name = "YourKit Profiler Agent";
    private MBeanServer mBeanServer;
    public static final String JMX_OBJECT_NAME = "yjpagent:name=YourKitProfiler";

    private JmxSupportFactory jmxSupportFactory = AutoDiscoveryJmxSupportFactory.getInstance();
    private JmxSupport jmxSupport = jmxSupportFactory.getJmxSupport();

    /**
     * Logger used by this class
     */
    protected static final Log logger = LogFactory.getLog(YourKitProfilerAgent.class);

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.manager.UMOAgent#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.manager.UMOAgent#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.manager.UMOAgent#getDescription()
     */
    public String getDescription()
    {
        return "YourKit Profiler JMX Agent";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.lifecycle.Initialisable#initialise()
     */
    public void initialise() throws InitialisationException
    {
        try
        {
            final List servers = MBeanServerFactory.findMBeanServer(null);
            if (servers.isEmpty())
            {
                throw new InitialisationException(ManagementMessages.noMBeanServerAvailable(), this);
            }

            mBeanServer = (MBeanServer) servers.get(0);

            final ObjectName objectName = jmxSupport.getObjectName(JMX_OBJECT_NAME);
            // unregister existing YourKit MBean first if required
            unregisterMBeansIfNecessary();
            mBeanServer.registerMBean(new YourKitProfilerService(), objectName);
        }
        catch (NoClassDefFoundError ncde)
        {
            if ("com/yourkit/api/Controller".equals(ncde.getMessage()))
            {
                logger.warn("Cannot find YourKit API. JMX Agent won't start.");
            }
            else
            {
                throw ncde;
            }
        }
        catch (Exception e)
        {
            throw new InitialisationException(CoreMessages.failedToStart("JMX Agent"), e, this);
        }
    }

    /**
     * Unregister all YourKit MBeans if there are any left over the old deployment
     */
    protected void unregisterMBeansIfNecessary()
        throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException
    {
        if (mBeanServer.isRegistered(jmxSupport.getObjectName(JMX_OBJECT_NAME)))
        {
            // unregister all yjpMBeans
            Set yjpMBeans = mBeanServer.queryMBeans(jmxSupport.getObjectName("yjpagent*:*"), null);
            for (Iterator it = yjpMBeans.iterator(); it.hasNext();)
            {
                ObjectInstance objectInstance = (ObjectInstance) it.next();
                ObjectName theName = objectInstance.getObjectName();
                mBeanServer.unregisterMBean(theName);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.lifecycle.Startable#start()
     */
    public void start() throws UMOException
    {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.lifecycle.Stoppable#stop()
     */
    public void stop() throws UMOException
    {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.lifecycle.Disposable#dispose()
     */
    public void dispose()
    {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.manager.UMOAgent#registered()
     */
    public void registered()
    {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.manager.UMOAgent#unregistered()
     */
    public void unregistered()
    {
        // nothing to do
    }

}
