/*
 * $Id: $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.registry.impl;

import java.util.Dictionary;

import org.mule.registry.impl.MuleRegistration;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class OSGIServiceRegistration extends MuleRegistration implements ServiceRegistration
{
    public ServiceReference getReference()
    {
        return null;
    }

    public void setProperties(Dictionary properties)
    {
    }

    public void unregister()
    {
    }
}
