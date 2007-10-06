/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.spring.parsers.assembly;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class MapBeanAssemblerFactory implements BeanAssemblerFactory
{

    private BeanAssemblerStore store;

    public MapBeanAssemblerFactory(BeanAssemblerStore store)
    {
        this.store = store;
    }

    public BeanAssembler newBeanAssembler(
            PropertyConfiguration beanConfig, BeanDefinitionBuilder bean,
            PropertyConfiguration targetConfig, BeanDefinition target)
    {
        return new MapBeanAssembler(store, beanConfig, bean, targetConfig, target);
    }

    public interface BeanAssemblerStore
    {

        public void saveBeanAssembler(BeanAssembler beanAssembler);

    }


}