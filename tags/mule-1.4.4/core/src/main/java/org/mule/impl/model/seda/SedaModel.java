/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.model.seda;

import org.mule.MuleManager;
import org.mule.config.PoolingProfile;
import org.mule.config.QueueProfile;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.model.AbstractModel;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMODescriptor;

/**
 * A Mule component service model that uses Seda principles to achieve high
 * throughput by queueing events for components and processing them concurrently.
 */
public class SedaModel extends AbstractModel
{

    /**
     * The time out used for taking from the Seda Queue.
     */
    private int queueTimeout = MuleManager.getConfiguration().getSynchronousEventTimeout();

    /**
     * Whether components in this model should be pooled or not.
     */
    private boolean enablePooling = true;

    /**
     * Whether to create a new component for every request.
     */
    protected boolean componentPerRequest = false;


    /**
     * the pooling configuration used when initialising the component described by
     * this descriptor.
     */
    protected PoolingProfile poolingProfile;

    /**
     * The queuing profile for events received for this component
     */
    protected QueueProfile queueProfile;


    public SedaModel()
    {
        super();
        poolingProfile = new PoolingProfile();
        queueProfile = new QueueProfile();
    }

    /**
     * Returns the model type name. This is a friendly identifier that is used to
     * look up the SPI class for the model
     * 
     * @return the model type
     */
    public String getType()
    {
        return "seda";
    }

    protected UMOComponent createComponent(UMODescriptor descriptor)
    {
        return new SedaComponent((MuleDescriptor) descriptor, this);
    }

    public int getQueueTimeout()
    {
        return queueTimeout;
    }

    public void setQueueTimeout(int queueTimeout)
    {
        this.queueTimeout = queueTimeout;
    }

    public boolean isEnablePooling()
    {
        return enablePooling;
    }

    public void setEnablePooling(boolean enablePooling)
    {
        this.enablePooling = enablePooling;
    }

    public boolean isComponentPerRequest()
    {
        return componentPerRequest;
    }

    public void setComponentPerRequest(boolean componentPerRequest)
    {
        this.componentPerRequest = componentPerRequest;
    }


    public PoolingProfile getPoolingProfile()
    {
        return poolingProfile;
    }

    public void setPoolingProfile(PoolingProfile poolingProfile)
    {
        this.poolingProfile = poolingProfile;
    }

    public QueueProfile getQueueProfile()
    {
        return queueProfile;
    }

    public void setQueueProfile(QueueProfile queueProfile)
    {
        this.queueProfile = queueProfile;
    }
}
