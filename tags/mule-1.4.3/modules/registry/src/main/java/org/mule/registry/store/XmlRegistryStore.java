/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.registry.store;

import org.mule.ManagementContext;
import org.mule.registry.Registry;
import org.mule.registry.RegistryException;
import org.mule.registry.RegistryFactory;
import org.mule.registry.RegistryStore;
import org.mule.registry.impl.AbstractRegistry;
import org.mule.util.FileUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class XmlRegistryStore implements RegistryStore
{

    protected ManagementContext context;

    public XmlRegistryStore(ManagementContext context)
    {
        this.context = context;
    }

    public void save(Registry registry) throws RegistryException
    {
        synchronized (registry)
        {
            try
            {
                Writer w = new FileWriter(FileUtils.newFile(registry.getStoreLocation()));
                getXStream().toXML(registry, w);
                w.close();
            }
            catch (Exception e)
            {
                throw new RegistryException("Could not save registry", e);
            }
        }
    }

    public Registry load(String storeLocation) throws RegistryException
    {
        try
        {
            Reader r = new FileReader(storeLocation);
            AbstractRegistry reg = (AbstractRegistry)getXStream().fromXML(r);
            reg.initialize();
            reg.setStoreLocation(storeLocation);
            r.close();
            return reg;
        }
        catch (IOException e)
        {
            throw new RegistryException("Could not load registry", e);
        }
    }

    public Registry create(String store, RegistryFactory factory) throws RegistryException
    {
        Registry reg = factory.create(this, context);
        if (reg instanceof AbstractRegistry)
        {
            ((AbstractRegistry)reg).initialize();
            ((AbstractRegistry)reg).setStoreLocation(store);
        }
        save(reg);
        return reg;
    }

    private static XStream getXStream()
    {
        if (xstream == null)
        {
            xstream = new XStream(new StaxDriver());
            // xstream.alias("registry", BaseRegistry.class);
            // xstream.alias("engine", EngineImpl.class);
            // xstream.alias("binding", BindingImpl.class);
            // xstream.alias("library", LibraryImpl.class);
            // xstream.alias("assembly", AssemblyImpl.class);
            // xstream.alias("unit", UnitImpl.class);
        }
        return xstream;
    }

    private static XStream xstream;
}
