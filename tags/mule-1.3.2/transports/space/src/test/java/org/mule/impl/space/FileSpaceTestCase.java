/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.space;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class FileSpaceTestCase extends AbstractLocalSpaceTestCase
{

    protected DefaultSpaceFactory getSpaceFactory() throws Exception
    {
        return new FileSpaceFactory(false);
    }

    protected boolean isPersistent()
    {
        return true;
    }
}
