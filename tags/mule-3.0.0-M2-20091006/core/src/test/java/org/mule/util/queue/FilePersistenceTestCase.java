/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.util.queue;

public class FilePersistenceTestCase extends AbstractTransactionQueueManagerTestCase
{

    protected TransactionalQueueManager createQueueManager() throws Exception
    {
        TransactionalQueueManager mgr = new TransactionalQueueManager();
        FilePersistenceStrategy ps = new FilePersistenceStrategy();
        ps.setMuleContext(muleContext);
        mgr.setPersistenceStrategy(ps);
        mgr.setDefaultQueueConfiguration(new QueueConfiguration(true));
        return mgr;
    }

    protected boolean isPersistent()
    {
        return true;
    }

}
