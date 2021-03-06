/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tck;

import org.mule.MuleManager;
import org.mule.config.MuleConfiguration;
import org.mule.impl.MuleDescriptor;
import org.mule.tck.testmodels.mule.TestConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOException;
import org.mule.umo.UMOSession;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.manager.UMOManager;
import org.mule.umo.model.UMOModel;
import org.mule.umo.transformer.UMOTransformer;
import org.mule.util.FileUtils;
import org.mule.util.MuleUrlStreamHandlerFactory;
import org.mule.util.StringMessageUtils;
import org.mule.util.StringUtils;
import org.mule.util.SystemUtils;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>AbstractMuleTestCase</code> is a base class for Mule testcases. This
 * implementation provides services to test code for creating mock and test objects.
 */
public abstract class AbstractMuleTestCase extends TestCase
{
    /**
     * This flag controls whether the text boxes will be logged when starting each test case.
     */
    private static final boolean verbose;
    
    protected final Log logger = LogFactory.getLog(this.getClass());

    // This should be set to a string message describing any prerequisites not met
    private boolean offline = System.getProperty("org.mule.offline", "false").equalsIgnoreCase("true");

    private static Map testCounters;
    
    private TestCaseWatchdog watchdog;

    static
    {
        String muleOpts = SystemUtils.getenv("MULE_TEST_OPTS");
        if (StringUtils.isNotBlank(muleOpts))
        {
            Map parsedOpts = SystemUtils.parsePropertyDefinitions(muleOpts);
            String optVerbose = (String)parsedOpts.get("mule.verbose");
            verbose = Boolean.valueOf(optVerbose).booleanValue();
        }
        else
        {
            // per default, revert to the old behaviour
            verbose = true;
        }

        // register the custom UrlStreamHandlerFactory.
        MuleUrlStreamHandlerFactory.installUrlStreamHandlerFactory();
    }
    
    public AbstractMuleTestCase()
    {
        super();
        if (testCounters == null)
        {
            testCounters = new HashMap();
        }
        addTest();
    }

    protected void addTest()
    {
        TestInfo info = (TestInfo) testCounters.get(getClass().getName());
        if (info == null)
        {
            info = new TestInfo(getClass().getName());
            testCounters.put(getClass().getName(), info);
        }
        info.incTestCount();
    }

    protected void setDisposeManagerPerSuite(boolean val)
    {
        getTestInfo().setDisposeManagerPerSuite(val);
    }

    protected TestInfo getTestInfo()
    {
        TestInfo info = (TestInfo) testCounters.get(getClass().getName());
        if (info == null)
        {
            info = new TestInfo(getClass().getName());
            testCounters.put(getClass().getName(), info);
        }
        return info;
    }

    private void clearAllCounters()
    {
        if (testCounters != null)
        {
            testCounters.clear();
        }
    }

    private void clearCounter()
    {
        if (testCounters != null)
        {
            testCounters.remove(getClass().getName());
        }
    }

    public String getName()
    {
        return super.getName().substring(4).replaceAll("([A-Z])", " $1").toLowerCase() + " ";
    }

    public void run(TestResult result) 
    {
        if (this.isDisabledInThisEnvironment())
        {
            logger.info(this.getClass().getName() + " disabled");
            return;
        }
        
        super.run(result);
    }
     
    /**
     * Subclasses can override this method to skip the execution of the entire test class.
     * 
     * @return <code>true</code> if the test class should not be run.
     */
    protected boolean isDisabledInThisEnvironment()
    {
        return false;
    }
    
    /**
     * Shamelessly copy from Spring's ConditionalTestCase so in MULE-2.0 we can extend
     * this class from ConditionalTestCase.
     * <p/>
     * Subclasses can override <code>isDisabledInThisEnvironment</code> to skip a single test.
     */
    public void runBare() throws Throwable 
    {
        // getName will return the name of the method being run. Use the real JUnit implementation,
        // this class has a different implementation
        if (this.isDisabledInThisEnvironment(super.getName())) 
        {
            logger.warn(this.getClass().getName() + "." + super.getName() + " disabled in this environment");
            return;
        }
        
        // Let JUnit handle execution
        super.runBare();
    }

    /**
     * Should this test run?
     * @param testMethodName name of the test method
     * @return whether the test should execute in the current envionment
     */
    protected boolean isDisabledInThisEnvironment(String testMethodName) 
    {
        return false;
    }

    public boolean isOffline(String method)
    {
        if (offline)
        {
            logger.warn(StringMessageUtils.getBoilerPlate(
                "Working offline cannot run test: " + method, '=', 80));
        }
        return offline;
    }

    protected final void setUp() throws Exception
    {
        // start a watchdog thread that kills the VM after 30 minutes timeout
        watchdog = new TestCaseWatchdog(30, TimeUnit.MINUTES);
        watchdog.start();
        
        if (verbose)
        {
            System.out.println(StringMessageUtils.getBoilerPlate("Testing: " + toString(), '=', 80));   
        }
        MuleManager.getConfiguration().getDefaultThreadingProfile().setDoThreading(false);
        MuleManager.getConfiguration().setServerUrl(StringUtils.EMPTY);

        try
        {
            if (getTestInfo().getRunCount() == 0)
            {
                if (getTestInfo().isDisposeManagerPerSuite())
                {
                    // We dispose here jut in case
                    disposeManager();
                }
                suitePreSetUp();
            }
            if (!getTestInfo().isDisposeManagerPerSuite())
            {
                // We dispose here jut in case
                disposeManager();
            }
            doSetUp();
            if (getTestInfo().getRunCount() == 0)
            {
                suitePostSetUp();
            }
        }
        catch (Exception e)
        {
            getTestInfo().incRunCount();
            throw e;
        }
    }

    protected void suitePreSetUp() throws Exception
    {
        // nothing to do
    }

    protected void suitePostSetUp() throws Exception
    {
        // nothing to do
    }

    protected void suitePreTearDown() throws Exception
    {
        // nothing to do
    }

    protected void suitePostTearDown() throws Exception
    {
        // nothing to do
    }

    protected final void tearDown() throws Exception
    {
        try
        {
            if (getTestInfo().getRunCount() == getTestInfo().getTestCount())
            {
                suitePreTearDown();
            }
            doTearDown();
            if (!getTestInfo().isDisposeManagerPerSuite())
            {
                disposeManager();
            }
        }
        finally
        {
            try
            {
                getTestInfo().incRunCount();
                if (getTestInfo().getRunCount() == getTestInfo().getTestCount())
                {
                    try
                    {
                        suitePostTearDown();
                    }
                    finally
                    {
                        clearCounter();
                        disposeManager();
                    }
                }
            }
            finally 
            {
                // remove the watchdog thread in any case
                watchdog.cancel();
            }
        }
    }

    protected void disposeManager() throws UMOException
    {
        if (MuleManager.isInstanciated())
        {
            MuleManager.getInstance().dispose();
        }
        FileUtils.deleteTree(FileUtils.newFile(MuleManager.getConfiguration().getWorkingDirectory()));
        FileUtils.deleteTree(FileUtils.newFile("./ActiveMQ"));
        MuleManager.setConfiguration(new MuleConfiguration());
    }

    protected void doSetUp() throws Exception
    {
        // template method
    }

    protected void doTearDown() throws Exception
    {
        // template method
    }

    public static UMOManager getManager(boolean disableAdminAgent) throws Exception
    {
        return MuleTestUtils.getManager(disableAdminAgent);
    }

    public static UMOModel getDefaultModel() throws UMOException
    {
        return MuleTestUtils.getDefaultModel();
    }

    public static UMOEndpoint getTestEndpoint(String name, String type) throws Exception
    {
        return MuleTestUtils.getTestEndpoint(name, type);
    }

    public static UMOEvent getTestEvent(Object data) throws Exception
    {
        return MuleTestUtils.getTestEvent(data);
    }

    public static UMOEventContext getTestEventContext(Object data) throws Exception
    {
        return MuleTestUtils.getTestEventContext(data);
    }

    public static UMOTransformer getTestTransformer()
    {
        return MuleTestUtils.getTestTransformer();
    }

    public static UMOEvent getTestEvent(Object data, MuleDescriptor descriptor) throws Exception
    {
        return MuleTestUtils.getTestEvent(data, descriptor);
    }

    public static UMOEvent getTestEvent(Object data, UMOImmutableEndpoint endpoint) throws Exception
    {
        return MuleTestUtils.getTestEvent(data, endpoint);
    }

    public static UMOEvent getTestEvent(Object data, MuleDescriptor descriptor, UMOImmutableEndpoint endpoint)
        throws UMOException
    {
        return MuleTestUtils.getTestEvent(data, descriptor, endpoint);
    }

    public static UMOSession getTestSession(UMOComponent component)
    {
        return MuleTestUtils.getTestSession(component);
    }

    public static TestConnector getTestConnector()
    {
        return MuleTestUtils.getTestConnector();
    }

    public static UMOComponent getTestComponent(MuleDescriptor descriptor)
    {
        return MuleTestUtils.getTestComponent(descriptor);
    }

    public static MuleDescriptor getTestDescriptor(String name, String implementation) throws Exception
    {
        return MuleTestUtils.getTestDescriptor(name, implementation);
    }

    public static UMOManager getTestManager() throws Exception
    {
        return MuleTestUtils.getManager(true);
    }

    protected void finalize() throws Throwable
    {
        try
        {
            clearAllCounters();
        }
        finally
        {
            super.finalize();
        }
    }

    protected class TestInfo
    {
        /**
         * Whether to dispose the manager after every method or once all tests for
         * the class have run
         */
        private boolean disposeManagerPerSuite = false;
        private int testCount = 0;
        private int runCount = 0;
        private String name;

        public TestInfo(String name)
        {
            this.name = name;
        }

        public void clearCounts()
        {
            testCount = 0;
            runCount = 0;
        }

        public void incTestCount()
        {
            testCount++;
        }

        public void incRunCount()
        {
            runCount++;
        }

        public int getTestCount()
        {
            return testCount;
        }

        public int getRunCount()
        {
            return runCount;
        }

        public String getName()
        {
            return name;
        }

        public boolean isDisposeManagerPerSuite()
        {
            return disposeManagerPerSuite;
        }

        public void setDisposeManagerPerSuite(boolean disposeManagerPerSuite)
        {
            this.disposeManagerPerSuite = disposeManagerPerSuite;
        }

        public String toString()
        {
            StringBuffer buf = new StringBuffer();
            return buf.append(name).append(", (").append(runCount).append(" / ").append(testCount).append(
                ") tests run, disposePerSuite=").append(disposeManagerPerSuite).toString();
        }
    }
}
