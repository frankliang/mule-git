/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.mule.commonspool;

import org.mule.config.pool.CommonsPoolProxyFactory;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.model.seda.SedaModel;
import org.mule.tck.model.AbstractProxyPoolFactoryTestCase;
import org.mule.tck.testmodels.fruit.Apple;
import org.mule.util.ObjectFactory;
import org.mule.util.ObjectPool;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */

public class CommonsPoolProxyFactoryTestCase extends AbstractProxyPoolFactoryTestCase
{
    public ObjectFactory getProxyFactory(MuleDescriptor descriptor, ObjectPool pool)
    {
        CommonsPoolProxyFactory factory = new CommonsPoolProxyFactory(descriptor, new SedaModel());
        factory.setPool(pool);
        return factory;
    }

    public void testLifeCycleMethods() throws Exception
    {
        getManager(true);
        Mock mockPool = new Mock(ObjectPool.class);
        mockPool.expect("onAdd", C.IS_NOT_NULL);
        mockPool.expect("onRemove", C.IS_NOT_NULL);
        MuleDescriptor descriptor = getTestDescriptor("apple", Apple.class.getName());
        CommonsPoolProxyFactory factory = (CommonsPoolProxyFactory)getProxyFactory(descriptor,
            (ObjectPool)mockPool.proxy());

        assertNotNull(factory);

        Object obj = factory.makeObject();
        assertNotNull(obj);
        assertTrue(factory.validateObject(obj));
        factory.activateObject(obj);
        factory.passivateObject(obj);
        factory.destroyObject(obj);
    }
}
