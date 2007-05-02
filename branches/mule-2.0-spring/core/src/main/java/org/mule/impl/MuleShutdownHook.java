/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.impl;

import org.mule.RegistryContext;
import org.mule.config.ExceptionHelper;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.config.spring.RegistryFacade;
import org.mule.umo.UMOException;
import org.mule.umo.UMOManagementContext;
import org.mule.util.StringMessageUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * The shutdown thread used by the server when its main thread is terminated
 */
public class MuleShutdownHook extends Thread
{
    private Log logger;
    private Throwable exception = null;

    public MuleShutdownHook(Log logger)
    {
        super();
        this.logger = logger;
    }

    public MuleShutdownHook(Log logger, Throwable exception)
    {
        this(logger);
        this.exception = exception;
    }

    /*
    * (non-Javadoc)
    *
    * @see java.lang.Runnable#run()
    */
    public void run()
    {
        if (exception != null)
        {
            shutdown(exception);
        }
        else
        {
            shutdown();
        }
    }

    protected void shutdown(Throwable t)
    {
        Message msg = new Message(Messages.FATAL_ERROR_WHILE_RUNNING);
        UMOException muleException = ExceptionHelper.getRootMuleException(t);
        if (muleException != null)
        {
            logger.fatal(muleException.getDetailedMessage());
        }
        else
        {
            logger.fatal(msg.toString() + " " + t.getMessage(), t);
        }
        List msgs = new ArrayList();
        msgs.add(msg.getMessage());
        Throwable root = ExceptionHelper.getRootException(t);
        msgs.add(root.getMessage() + " (" + root.getClass().getName() + ")");
        msgs.add(" ");
        msgs.add(new Message(Messages.FATAL_ERROR_SHUTDOWN));
        UMOManagementContext context = RegistryContext.getRegistry().getManagementContext();
        if(context!=null)
        {
            msgs.add(new Message(Messages.SERVER_STARTED_AT_X, new Date(context.getStartDate())));
        }
        msgs.add(new Message(Messages.SERVER_SHUTDOWN_AT_X, new Date().toString()));

        String shutdownMessage = StringMessageUtils.getBoilerPlate(msgs, '*', 80);

        if (logger.isFatalEnabled())
        {
            logger.fatal(shutdownMessage);
        }
        else
        {
            System.err.println(shutdownMessage);
        }
    }

    protected void shutdown()
    {
        logger.info("Mule server shutting down due to normal shutdown request");
        List msgs = new ArrayList();
        msgs.add(new Message(Messages.NORMAL_SHUTDOWN).getMessage());

        UMOManagementContext context = null;
        RegistryFacade registry = RegistryContext.getRegistry();

        if(registry!=null)
        {
            context = registry.getManagementContext();
        }
        if(context!=null)
        {
            msgs.add(new Message(Messages.SERVER_STARTED_AT_X, new Date(context.getStartDate())));
        }
        msgs.add(new Message(Messages.SERVER_SHUTDOWN_AT_X, new Date().toString()).getMessage());
        String shutdownMessage = StringMessageUtils.getBoilerPlate(msgs, '*', 80);
        if (logger.isInfoEnabled())
        {
            logger.info(shutdownMessage);
        }
        else
        {
            System.out.println(shutdownMessage);
        }
    }

    public Throwable getException()
    {
        return exception;
    }

    public void setException(Throwable exception)
    {
        this.exception = exception;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o != null && getClass().getName().equals(o.getClass().getName()))
        {
            return true;
        }

        return false;
    }

    public int hashCode()
    {
        return getClass().getName().hashCode();
    }
}

