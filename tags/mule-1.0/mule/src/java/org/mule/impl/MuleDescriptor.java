/* 
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */

package org.mule.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleException;
import org.mule.MuleManager;
import org.mule.config.MuleConfiguration;
import org.mule.config.PoolingProfile;
import org.mule.config.QueueProfile;
import org.mule.config.ThreadingProfile;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.routing.inbound.InboundMessageRouter;
import org.mule.routing.inbound.InboundPassThroughRouter;
import org.mule.routing.outbound.OutboundMessageRouter;
import org.mule.routing.outbound.OutboundPassThroughRouter;
import org.mule.umo.UMODescriptor;
import org.mule.umo.UMOInterceptor;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.Initialisable;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.routing.UMOInboundMessageRouter;
import org.mule.umo.routing.UMOOutboundMessageRouter;
import org.mule.umo.routing.UMOOutboundRouter;
import org.mule.umo.routing.UMOResponseMessageRouter;
import org.mule.umo.transformer.UMOTransformer;

import java.beans.ExceptionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * <code>MuleDescriptor</code>  describes all the properties for a Mule UMO.  New Mule UMOs
 * can be initialised as needed from their descriptor.
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */

public class MuleDescriptor extends ImmutableMuleDescriptor implements UMODescriptor
{
    public static final String DEFAULT_INSTANCE_REF_NAME = "_instanceRef";
    /**
     * logger used by this class
     */
    private static transient Log logger = LogFactory.getLog(MuleDescriptor.class);

    public MuleDescriptor(String name)
    {
        super();
        this.name = name;
    }

    public MuleDescriptor(MuleDescriptor descriptor)
    {
        super(descriptor);
    }


    /**
     * Default constructor. Initalises common properties for the MuleConfiguration object
     *
     * @see MuleConfiguration
     */
    public MuleDescriptor()
    {
        super();
    }

    public void setThreadingProfile(ThreadingProfile threadingProfile)
    {
        this.threadingProfile = threadingProfile;
    }

    /* (non-Javadoc)
     * @see org.mule.umo.UMODescriptor#setExceptionListener(org.mule.umo.UMOExceptionStrategy)
     */
    public void setExceptionListener(ExceptionListener listener)
    {
        if (listener == null) throw new IllegalArgumentException("Exception Strategy cannot be null");
        this.exceptionListener = listener;
        logger.debug("Using exception strategy: " + listener.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.mule.umo.UMODescriptor#setName(java.lang.String)
     */
    public void setName(String newName)
    {
        if (newName == null) throw new IllegalArgumentException("Name cannot be null");
        name = newName;
    }

    /* (non-Javadoc)
     * @see org.mule.transformers.HasTransformer#setOutboundTransformer(org.mule.umo.transformer.UMOTransformer)
     */
    public void setOutboundTransformer(UMOTransformer transformer)
    {
        outboundTransformer = transformer;
    }

    /* (non-Javadoc)
     * @see org.mule.umo.UMODescriptor#getPropertiesForURI(java.util.Properties)
     */
    public void setProperties(HashMap props)
    {
        properties = props;
        String delegate = (String) properties.get(MULE_PROPERTY_DOT_PROPERTIES);
        if (delegate != null)
        {
            try
            {
                FileInputStream is = new FileInputStream(new File(delegate));
                Properties dProps = new Properties();
                dProps.load(is);
                properties.putAll(dProps);
            }
            catch (Exception e)
            {
                logger.warn(MULE_PROPERTY_DOT_PROPERTIES
                        + " was set  to "
                        + delegate
                        + " but the file could not be read, exception is: "
                        + e.getMessage());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.mule.umo.UMODescriptor#setVersion(long)
     */
    public void setVersion(String ver)
    {
        version = ver;
    }

    /* (non-Javadoc)
     * @see org.mule.umo.UMODescriptor#setInboundEndpoint(org.mule.impl.UMOEndpoint)
     */
    public void setInboundEndpoint(UMOEndpoint endpoint) throws MuleException
    {
        inboundEndpoint = endpoint;
        if(inboundEndpoint!=null) {
            inboundEndpoint.setType(UMOEndpoint.ENDPOINT_TYPE_RECEIVER);
            if(inboundEndpoint.getTransformer()!=null) inboundTransformer = inboundEndpoint.getTransformer();
        }
    }


    /* (non-Javadoc)
     * @see org.mule.umo.UMODescriptor#setOutboundEndpoint(org.mule.impl.UMO
     * ProviderDescriptor)
     */
    public void setOutboundEndpoint(UMOEndpoint endpoint) throws MuleException
    {
        outboundEndpoint = endpoint;
        if(outboundEndpoint!=null) {
            outboundEndpoint.setType(UMOEndpoint.ENDPOINT_TYPE_SENDER);
            if(outboundEndpoint.getTransformer()!=null) outboundTransformer = outboundEndpoint.getTransformer();
        }

    }


    /* (non-Javadoc)
     * @see org.mule.transformers.HasTransformer#setInboundTransformer(org.mule.umo.transformer.UMOTransformer)
     */
    public void setInboundTransformer(UMOTransformer transformer)
    {
        inboundTransformer = transformer;
    }

    /* (non-Javadoc)
     * @see org.mule.umo.UMODescriptor#addinteceptor(org.mule.umo.UMOInterceptor)
     */
    public void addInterceptor(UMOInterceptor inteceptor)
    {
        if (inteceptor != null)
            intecerptorList.add(inteceptor);
    }
    
    public void setInterceptors(List inteceptorList)
    {
        this.intecerptorList = inteceptorList;
    }


    /* (non-Javadoc)
     * @see org.mule.umo.UMODescriptor#setPoolingProfile(UMOPoolingProfile)
     */
    public void setPoolingProfile(PoolingProfile poolingProfile)
    {
        this.poolingProfile = poolingProfile;
    }

    public void setQueueProfile(QueueProfile queueProfile)
    {
        this.queueProfile = queueProfile;
    }

    /* (non-Javadoc)
     * @see org.mule.umo.UMODescriptor#setImplementation(java.lang.String)
     */
    public void setImplementation(Object reference)
    {
        if (reference == null) throw new IllegalArgumentException("ImplementationReference cannot be null");
        implementationReference = reference;
    }

    public void setImplementationInstance(Object instance)
    {
        properties.put(DEFAULT_INSTANCE_REF_NAME, instance);
        setImplementation(MuleDescriptor.IMPLEMENTATION_TYPE_LOCAL + DEFAULT_INSTANCE_REF_NAME);
    }

    public void setInboundRouter(UMOInboundMessageRouter routerList)
    {
        this.inboundRouter = routerList;
    }

    public void setOutboundRouter(UMOOutboundMessageRouter routerList)
    {
        outboundRouter = routerList;
    }

    public void setContainerManaged(boolean value) {
        containerManaged = value;
    }


    public void initialise() throws InitialisationException
    {
        MuleConfiguration config = MuleManager.getConfiguration();
        if(threadingProfile==null) threadingProfile = config.getComponentThreadingProfile();
        if(poolingProfile==null) poolingProfile = config.getPoolingProfile();
        if(queueProfile==null) queueProfile = config.getQueueProfile();

        if(exceptionListener==null) {
            exceptionListener = MuleManager.getInstance().getModel().getExceptionListener();
        } else if(exceptionListener instanceof Initialisable) {
            ((Initialisable)exceptionListener).initialise();
        }

        if(inboundEndpoint!=null) {
            if(inboundTransformer!=null) {
                inboundEndpoint.setTransformer(inboundTransformer);
            }
            ((MuleEndpoint)inboundEndpoint).initialise();
            //If the transformer was set on the endpoint uri, it will only
            //be initialised when the endpoint is initialised, hence we make
            //this call here to ensure a consistent state
            if(inboundTransformer==null) {
               inboundTransformer = inboundEndpoint.getTransformer();
            }
        }

        if(outboundEndpoint!=null) {
            if(outboundTransformer!=null) outboundEndpoint.setTransformer(outboundTransformer);
            ((MuleEndpoint)outboundEndpoint).initialise();
            //If the transformer was set on the endpoint uri, it will only
            //be initialised when the endpoint is initialised, hence we make
            //this call here to ensure a consistent state
            if(outboundTransformer==null) {
               outboundTransformer = outboundEndpoint.getTransformer();
            }
        }

        if(exceptionListener instanceof Initialisable) {
            ((Initialisable)exceptionListener).initialise();
        }

        MuleEndpoint endpoint;
        if(inboundRouter==null) {
            //Create Default routes that route to the default inbound and outbound endpoints
            inboundRouter = new InboundMessageRouter();
            inboundRouter.addRouter(new InboundPassThroughRouter());
        } else {
            if(inboundRouter.getCatchAllStrategy() != null &&
                    inboundRouter.getCatchAllStrategy().getEndpoint()!=null) {
                ((MuleEndpoint)inboundRouter.getCatchAllStrategy().getEndpoint()).initialise();
            }
            for (Iterator iterator = inboundRouter.getEndpoints().iterator(); iterator.hasNext();)
            {
                endpoint = (MuleEndpoint) iterator.next();
                endpoint.initialise();
            }
        }

        if(responseRouter!=null) {
            for (Iterator iterator = responseRouter.getEndpoints().iterator(); iterator.hasNext();)
            {
                endpoint = (MuleEndpoint) iterator.next();
                endpoint.initialise();
            }
        }

        if(outboundRouter==null) {
            outboundRouter = new OutboundMessageRouter();
            outboundRouter.addRouter(new OutboundPassThroughRouter(this));
        } else {
            if(outboundRouter.getCatchAllStrategy() != null &&
                    outboundRouter.getCatchAllStrategy().getEndpoint()!=null) {
                ((MuleEndpoint)outboundRouter.getCatchAllStrategy().getEndpoint()).initialise();
            }
            UMOOutboundRouter router = null;
            for (Iterator iterator = outboundRouter.getRouters().iterator(); iterator.hasNext();)
            {
                router = (UMOOutboundRouter) iterator.next();
                for (Iterator iterator1 = router.getEndpoints().iterator(); iterator1.hasNext();)
                {
                    endpoint = (MuleEndpoint)iterator1.next();
                    endpoint.initialise();
                }
            }
        }
    }

    public void addInitialisationCallback(InitialisationCallback callback)
    {
        initialisationCallbacks.add(callback);
    }

    /**
     * Response Routers control how events are returned in a request/response call.
     * It cn be use to aggregate response events before returning, thus acting as a
     * Join in a forked process.  This can be used to make request/response calls a lot
     * more efficient as independent tasks can be forked, execute concurrently and then
     * join before the request completes
     *
     * @param router the response router for this component
     * @see org.mule.umo.routing.UMOResponseMessageRouter
     */
    public void setResponseRouter(UMOResponseMessageRouter router)
    {
        this.responseRouter = router;
    }
}
