/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.management.agents;

import org.mule.api.context.MuleContextBuilder;
import org.mule.config.DefaultMuleConfiguration;
import org.mule.module.management.agent.FixedHostRmiClientSocketFactory;
import org.mule.module.management.agent.JmxAgent;
import org.mule.module.management.agent.RmiRegistryAgent;
import org.mule.tck.AbstractMuleTestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

public class JmxAgentTestCase extends AbstractMuleTestCase
{

    private static final String[] VALID_AUTH_TOKEN = {"mule", "mulepassword"};
    private static final String DOMAIN = "JmxAgentTest";
    
    private JMXServiceURL serviceUrl;
    private JmxAgent jmxAgent;

    @Override
    protected void configureMuleContext(MuleContextBuilder contextBuilder)
    {
        super.configureMuleContext(contextBuilder);

        DefaultMuleConfiguration config = new DefaultMuleConfiguration();
        config.setId(DOMAIN);
        contextBuilder.setMuleConfiguration(config);
    }

    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        serviceUrl = new JMXServiceURL(JmxAgent.DEFAULT_REMOTING_URI);
        muleContext.getRegistry().registerAgent(new RmiRegistryAgent());
        jmxAgent = new JmxAgent();
        jmxAgent.setConnectorServerUrl(JmxAgent.DEFAULT_REMOTING_URI);
    }

    protected void doTearDown()
    {
        jmxAgent.dispose();
    }

    public void testDefaultProperties() throws Exception
    {
        jmxAgent.setCredentials(getValidCredentials());
        muleContext.getRegistry().registerAgent(jmxAgent);
        muleContext.start();
    }

    public void testSuccessfulRemoteConnection() throws Exception
    {
        configureProperties();
        jmxAgent.setCredentials(getValidCredentials());
        muleContext.getRegistry().registerAgent(jmxAgent);
        muleContext.start();

        JMXConnector connector = null;
        try
        {
            Map props = Collections.singletonMap(JMXConnector.CREDENTIALS, VALID_AUTH_TOKEN);
            connector = JMXConnectorFactory.connect(serviceUrl, props);
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            // is it the right server?
            assertTrue(Arrays.asList(connection.getDomains()).toString(),
                    Arrays.asList(connection.getDomains()).contains("Mule." + DOMAIN));
        }
        finally
        {
            if (connector != null)
            {
                connector.close();
            }
        }
    }

    public void testNoCredentialsProvided() throws Exception
    {
        configureProperties();
        jmxAgent.setCredentials(getValidCredentials());
        muleContext.getRegistry().registerAgent(jmxAgent);
        muleContext.start();

        JMXConnector connector = null;
        try
        {
            connector = JMXConnectorFactory.connect(serviceUrl);
            fail("expected SecurityException");
        }
        catch (SecurityException e)
        {
            // expected
        }
        finally
        {
            if (connector != null)
            {
                connector.close();
            }            
        }
    }

    public void testNonRestrictedAccess() throws Exception
    {
        configureProperties();
        jmxAgent.setCredentials(null);
        muleContext.getRegistry().registerAgent(jmxAgent);
        muleContext.start();

        JMXConnector connector = null;
        try
        {
            connector = JMXConnectorFactory.connect(serviceUrl);
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            // is it the right server?
            assertTrue(Arrays.asList(connection.getDomains()).contains("Mule." + DOMAIN));
        }
        finally
        {
            if (connector != null)
            {
                connector.close();
            }
        }
    }

    protected Map getValidCredentials()
    {
        final Map credentials = new HashMap(1);
        credentials.put(VALID_AUTH_TOKEN[0], VALID_AUTH_TOKEN[1]);

        return credentials;
    }

    protected void configureProperties()
    {
        // make multi-NIC dev box happy by sticking RMI clients to a single
        // local ip address
        Map props = new HashMap();
        props.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,
                  new FixedHostRmiClientSocketFactory("127.0.0.1"));
        jmxAgent.setConnectorServerProperties(props);
    }
    
}
