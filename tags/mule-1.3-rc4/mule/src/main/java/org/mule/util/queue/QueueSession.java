/* 
 * $Id$
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package org.mule.util.queue;

import org.mule.util.xa.ResourceManagerException;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision$
 */
public interface QueueSession
{

    Queue getQueue(String name);

    void begin() throws ResourceManagerException;

    void commit() throws ResourceManagerException;

    void rollback() throws ResourceManagerException;

}
