/*
 * $Id $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.umo.manager;

import org.mule.umo.lifecycle.Lifecycle;

import javax.resource.spi.work.WorkManager;

/**
 * <code>UMOWorkManager</code> Extends the stanard WorkManager to add lifecycle methods
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public interface UMOWorkManager extends WorkManager, Lifecycle
{
    // no methods
}
