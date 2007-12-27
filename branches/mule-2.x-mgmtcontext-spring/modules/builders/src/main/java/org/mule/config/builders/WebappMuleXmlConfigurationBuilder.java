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

import org.mule.config.ConfigurationException;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.util.FileUtils;
import org.mule.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>WebappMuleXmlConfigurationBuilder</code> will first try and load config
 * resources from the Servlet context. If this fails it fails back to the methods
 * used by the MuleXmlConfigurationBuilder.
 *
 * @see org.mule.config.builders.SpringXmlConfigurationBuilder
 */
public class WebappMuleXmlConfigurationBuilder extends SpringXmlConfigurationBuilder
{
    /**
     * Logger used by this class
     */
    protected transient final Log logger = LogFactory.getLog(getClass());
    private ServletContext context;

    /**
     * Classpath within the servlet context (e.g., "WEB-INF/classes").  Mule will attempt to load config
     * files from here first, and then from the remaining classpath.
     */
    private String webappClasspath;

    public WebappMuleXmlConfigurationBuilder(ServletContext context, String webappClasspath)
            throws ConfigurationException
    {
        super();
        this.context = context;
        this.webappClasspath = webappClasspath;
    }

    /**
     * Attempt to load any resource from the Servlet Context first, then from FS and the classpath.
     */
    //@Override
    protected InputStream loadConfig(String configResource) throws ConfigurationException
    {
        String resourcePath = configResource;
        InputStream is = null;
        if (webappClasspath != null)
        {
            resourcePath = new File(webappClasspath, configResource).getPath();
            is = context.getResourceAsStream(resourcePath);
        }
        if (is == null)
        {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configResource);
        }
        if (logger.isDebugEnabled())
        {
            if (is != null)
            {
                logger.debug("Resource " + configResource + " is found in Servlet Context.");
            }
            else
            {
                logger.debug("Resource " + resourcePath + " is not found in Servlet Context, loading from classpath or as external file");
            }
        }
        if (is == null && webappClasspath != null)
        {
            resourcePath = FileUtils.newFile(webappClasspath, configResource).getPath();
            try
            {
                is = IOUtils.getResourceAsStream(resourcePath,getClass());
            }
            catch (IOException ex)
            {
                logger.debug("Resource " + resourcePath + " is not found in filesystem "+ex);
            }
        }
        if (is == null)
        {
            try
            {
                logger.debug("Resource " + resourcePath + " not found in Servlet Context, loading from classpath");
                is = IOUtils.getResourceAsStream(configResource, getClass());
            }
            catch (IOException ioex)
            {
                throw new ConfigurationException(CoreMessages.cannotLoadFromClasspath(configResource), ioex);
            }
        }
        return is;
    }
}
