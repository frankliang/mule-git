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

 *

 */

package org.mule.providers.stream;

import org.mule.umo.UMOException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

/**
 * <code>StreamMessageDispatcherFactory</code> TODO
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class StreamMessageDispatcherFactory implements UMOMessageDispatcherFactory
{
    /* (non-Javadoc)
     * @see org.mule.umo.provider.UMOMessageDispatcherFactory#create(org.mule.umo.provider.UMOConnector)
     */
    public UMOMessageDispatcher create(UMOConnector connector) throws UMOException
    {
        return new StreamMessageDispatcher((StreamConnector)connector);
    }
}