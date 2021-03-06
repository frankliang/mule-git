/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.transaction;

import org.mule.module.jboss.transaction.JBossArjunaTransactionManagerFactory;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.util.xa.AbstractTransactionContext;
import org.mule.util.xa.AbstractXAResourceManager;
import org.mule.util.xa.DefaultXASession;
import org.mule.util.xa.ResourceManagerException;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XAResourceManagerTestCase extends AbstractMuleTestCase
{
    private TransactionManager tm;

    protected void doSetUp() throws Exception
    {
        tm = new JBossArjunaTransactionManagerFactory().create(muleContext.getConfiguration());
    }

    protected void doTearDown() throws Exception
    {
        tm = null;
    }

    public void testTxBehaviour() throws Exception
    {
        TestXAResourceManager rm = new TestXAResourceManager();
        rm.start();
        DefaultXASession s = rm.createSession();

        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(s);

        tx.delistResource(s, XAResource.TMSUCCESS);
        tx.commit();

    }

    protected static class TestXAResourceManager extends AbstractXAResourceManager
    {

        private static Log logger = LogFactory.getLog(TestXAResourceManager.class);

        public DefaultXASession createSession()
        {
            return new DefaultXASession(this);
        }

        protected Log getLogger()
        {
            return logger;
        }

        protected AbstractTransactionContext createTransactionContext(Object session)
        {
            return new AbstractTransactionContext() 
            {
            };
        }

        protected void doBegin(AbstractTransactionContext context)
        {
            // template method
        }

        protected int doPrepare(AbstractTransactionContext context)
        {
            // template method
            return 0;
        }

        protected void doCommit(AbstractTransactionContext context) throws ResourceManagerException
        {
            // template method
        }

        protected void doRollback(AbstractTransactionContext context) throws ResourceManagerException
        {
            // template method
        }
    }
}
