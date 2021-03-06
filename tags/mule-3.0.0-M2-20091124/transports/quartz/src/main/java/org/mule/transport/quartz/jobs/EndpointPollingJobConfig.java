/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.quartz.jobs;

import org.mule.transport.quartz.config.AbstractJobConfig;

/**
 * Configuration for the {@link EndpointPollingJob} job.
 */
public class EndpointPollingJobConfig extends AbstractJobConfig
{
    private String endpointRef;

    //TODO this should use th system default
    private int timeout = 5000;

    public String getEndpointRef()
    {
        return endpointRef;
    }

    public void setEndpointRef(String endpointRef)
    {
        this.endpointRef = endpointRef;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    protected Class getStatefulJobClass()
    {
        return StatefulEndpointPollingJob.class;
    }

    protected Class getStatelessJobClass()
    {
        return EndpointPollingJob.class;
    }
}