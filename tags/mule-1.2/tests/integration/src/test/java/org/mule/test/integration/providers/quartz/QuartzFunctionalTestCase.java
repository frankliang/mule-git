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

package org.mule.test.integration.providers.quartz;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import org.mule.MuleManager;
import org.mule.config.ConfigurationBuilder;
import org.mule.config.builders.MuleXmlConfigurationBuilder;
import org.mule.extras.client.MuleClient;
import org.mule.providers.quartz.jobs.MuleClientReceiveJob;
import org.mule.providers.quartz.QuartzConnector;
import org.mule.tck.NamedTestCase;
import org.mule.util.concurrent.CountDownLatch;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class QuartzFunctionalTestCase extends NamedTestCase
{

    protected static CountDownLatch countDown;

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        if (MuleManager.isInstanciated()) {
            MuleManager.getInstance().dispose();
        }
        super.tearDown();
    }


    public void testMuleClientDispatchJob() throws Exception
    {
        countDown = new CountDownLatch(3);
        ConfigurationBuilder configBuilder = new MuleXmlConfigurationBuilder();
        configBuilder.configure("org/mule/test/integration/providers/quartz/quartz-dispatch.xml");
        new MuleClient().send("vm://quartz.scheduler", "quartz test", null);
        assertTrue(countDown.tryLock(5000, TimeUnit.MILLISECONDS));
    }

    public void testMuleClientReceiveAndDispatchJob() throws Exception
    {
        countDown = new CountDownLatch(3);
        ConfigurationBuilder configBuilder = new MuleXmlConfigurationBuilder();
        configBuilder.configure("org/mule/test/integration/providers/quartz/quartz-receive-dispatch.xml");

        new MuleClient().send("vm://event.queue", "quartz test", null);
        new MuleClient().send("vm://event.queue", "quartz test", null);
        new MuleClient().send("vm://event.queue", "quartz test", null);

        new MuleClient().send("vm://quartz.scheduler", "test", null);
        assertTrue(countDown.tryLock(5000, TimeUnit.MILLISECONDS));
    }

    public void testMuleClientReceiveAndDispatchUsingDelegatingJobAsPayload() throws Exception
    {
        countDown = new CountDownLatch(3);
        ConfigurationBuilder configBuilder = new MuleXmlConfigurationBuilder();
        configBuilder.configure("org/mule/test/integration/providers/quartz/quartz-receive-dispatch-delegating-job.xml");

        new MuleClient().send("vm://event.queue", "quartz test", null);
        new MuleClient().send("vm://event.queue", "quartz test", null);
        new MuleClient().send("vm://event.queue", "quartz test", null);

        Map props = new HashMap();
        props.put(QuartzConnector.PROPERTY_JOB_RECEIVE_ENDPOINT, "vm://event.queue");
        props.put(QuartzConnector.PROPERTY_JOB_DISPATCH_ENDPOINT, "vm://quartz.in");
        Object payload = new MuleClientReceiveJob();
        new MuleClient().send("vm://quartz.scheduler", payload, props);
        assertTrue(countDown.tryLock(5000, TimeUnit.MILLISECONDS));
    }

    public void testMuleClientReceiveAndDispatchUsingDelegatingJobAsProperty() throws Exception
    {
        countDown = new CountDownLatch(3);
        ConfigurationBuilder configBuilder = new MuleXmlConfigurationBuilder();
        configBuilder.configure("org/mule/test/integration/providers/quartz/quartz-receive-dispatch-delegating-job.xml");

        new MuleClient().send("vm://event.queue", "quartz test", null);
        new MuleClient().send("vm://event.queue", "quartz test", null);
        new MuleClient().send("vm://event.queue", "quartz test", null);

        Map props = new HashMap();
        props.put(QuartzConnector.PROPERTY_JOB_RECEIVE_ENDPOINT, "vm://event.queue");
        props.put(QuartzConnector.PROPERTY_JOB_DISPATCH_ENDPOINT, "vm://quartz.in");
        props.put(QuartzConnector.PROPERTY_JOB_OBJECT, new MuleClientReceiveJob());

        new MuleClient().send("vm://quartz.scheduler", "test", props);
        assertTrue(countDown.tryLock(5000, TimeUnit.MILLISECONDS));
    }

    public void testMuleClientReceiveAndDispatchUsingDelegatingJobAsPropertyRef() throws Exception
    {
        countDown = new CountDownLatch(3);
        ConfigurationBuilder configBuilder = new MuleXmlConfigurationBuilder();
        configBuilder.configure("org/mule/test/integration/providers/quartz/quartz-receive-dispatch-delegating-job.xml");

        new MuleClient().send("vm://event.queue", "quartz test", null);
        new MuleClient().send("vm://event.queue", "quartz test", null);
        new MuleClient().send("vm://event.queue", "quartz test", null);

        Map props = new HashMap();
        props.put(QuartzConnector.PROPERTY_JOB_RECEIVE_ENDPOINT, "vm://event.queue");
        props.put(QuartzConnector.PROPERTY_JOB_DISPATCH_ENDPOINT, "vm://quartz.in");
        //The ref will be loaded from the default classloader container context, but his
        //could be Jndi or spring
        props.put(QuartzConnector.PROPERTY_JOB_REF, MuleClientReceiveJob.class.getName());

        new MuleClient().send("vm://quartz.scheduler", "test", props);
        assertTrue(countDown.tryLock(5000, TimeUnit.MILLISECONDS));
    }

}
