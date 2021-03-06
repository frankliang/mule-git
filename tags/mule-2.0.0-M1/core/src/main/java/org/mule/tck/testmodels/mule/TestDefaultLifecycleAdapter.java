/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tck.testmodels.mule;

import org.mule.impl.DefaultLifecycleAdapter;
import org.mule.umo.UMODescriptor;
import org.mule.umo.UMOException;
import org.mule.umo.model.UMOEntryPointResolver;

/**
 * <code>TestDefaultLifecycleAdapter</code> TODO (document class)
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class TestDefaultLifecycleAdapter extends DefaultLifecycleAdapter
{
    /**
     * @param component
     * @param descriptor
     * @throws UMOException
     */
    public TestDefaultLifecycleAdapter(Object component, UMODescriptor descriptor) throws UMOException
    {
        super(component, descriptor);
    }

    /**
     * @param component
     * @param descriptor
     * @param epResolver
     * @throws UMOException
     */
    public TestDefaultLifecycleAdapter(Object component,
                                       UMODescriptor descriptor,
                                       UMOEntryPointResolver epResolver) throws UMOException
    {
        super(component, descriptor, epResolver);
    }

}
