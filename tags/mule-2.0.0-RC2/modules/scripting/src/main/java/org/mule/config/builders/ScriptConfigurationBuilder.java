/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.builders;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.components.script.jsr223.Scriptable;
import org.mule.config.builders.AbstractResourceConfigurationBuilder;
import org.mule.config.builders.i18n.BuildersMessages;

import javax.script.Bindings;

/** Configures Mule from one or more script files. */
public class ScriptConfigurationBuilder extends AbstractResourceConfigurationBuilder
{
    public static final String SCRIPT_ENGINE_NAME_PROPERTY = "org.mule.script.engine";

    private Scriptable scriptComponent = new Scriptable();

    protected MuleContext muleContext = null;
    protected boolean initialised = false;

    public ScriptConfigurationBuilder(String configResource) throws MuleException
    {
        this(System.getProperty(SCRIPT_ENGINE_NAME_PROPERTY), configResource);
    }

    public ScriptConfigurationBuilder(String[] configResources) throws MuleException
    {
        this(System.getProperty(SCRIPT_ENGINE_NAME_PROPERTY), configResources);
    }

    public ScriptConfigurationBuilder(String scriptEngineName, String configResource) throws MuleException
    {
        super(configResource);
        if (scriptEngineName == null)
        {
            throw new IllegalArgumentException(BuildersMessages.systemPropertyNotSet(
                SCRIPT_ENGINE_NAME_PROPERTY).getMessage());
        }
        scriptComponent.setScriptEngineName(scriptEngineName);
    }

    public ScriptConfigurationBuilder(String scriptEngineName, String[] configResources) throws MuleException
    {
        super(configResources);
        if (scriptEngineName == null)
        {
            throw new IllegalArgumentException(BuildersMessages.systemPropertyNotSet(
                SCRIPT_ENGINE_NAME_PROPERTY).getMessage());
        }
        scriptComponent.setScriptEngineName(scriptEngineName);
    }

    protected void doConfigure(MuleContext muleContext) throws Exception
    {
        for (int i = 0; i < configResources.length; i++)
        {
            String configResource = configResources[i];
            scriptComponent.setScriptFile(configResource);
            scriptComponent.initialise();
            Bindings ns = scriptComponent.getScriptEngine().createBindings();
            populateBindings(ns);
            scriptComponent.runScript(ns);
        }
    }

    protected void populateBindings(Bindings bindings)
    {
        bindings.put("muleContext", muleContext);
    }

}
