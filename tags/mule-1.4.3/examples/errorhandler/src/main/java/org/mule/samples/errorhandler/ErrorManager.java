/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.samples.errorhandler;

import org.mule.MuleManager;
import org.mule.samples.errorhandler.handlers.DefaultHandler;
import org.mule.samples.errorhandler.handlers.FatalHandler;
import org.mule.umo.UMOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <code>ErrorManager</code> TODO (document class)
 */
public class ErrorManager
{
    /** logger used by this class */
    private static transient Log logger = LogFactory.getLog(ErrorManager.class);

    private Map handlers = new HashMap();
    private ExceptionHandler defaultHandler = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.impl.MuleUMO#initialise(java.util.Properties)
     */
    public ErrorManager()
    {
        defaultHandler = new DefaultHandler();
    }

    public void setHandlers(ExceptionHandler[] eh)
    {
        for (int i = 0; i < eh.length; i++)
        {
            addHandler(eh[i]);
        }
    }

    public void addHandler(ExceptionHandler eh)
    {
        for (Iterator i = eh.getRegisteredClasses(); i.hasNext();)
        {
            handlers.put(i.next(), eh);
        }
    }

    public ExceptionHandler getHandler(Class exceptionClass)
    {
        Object obj = handlers.get(exceptionClass);
        if (obj == null)
        {
            obj = handlers.get(Throwable.class);
        }

        return (ExceptionHandler)obj;
    }

    public void onException(ErrorMessage msg) throws UMOException
    {
        Class eClass = null;
        ExceptionHandler eh = null;

        try
        {
            eClass = msg.getException().toException().getClass();
            eh = getHandler(eClass);
            eh.onException(msg);
        }
        catch (Exception e)
        {
        
            logger.error(LocaleMessage.handlerFailure(eh));

            if (eh instanceof DefaultHandler)
            {
                logger.error(LocaleMessage.defaultFatalHandling(FatalHandler.class));
                handleFatal(e);

            }
            else if (eh instanceof FatalHandler)
            {
                logger.fatal(LocaleMessage.fatalHandling(e));
                ((MuleManager)MuleManager.getInstance()).shutdown(e, false);
            }
            else
            {
                logger.error(LocaleMessage.defaultHandling(DefaultHandler.class, eh, e));
                handleDefault(msg, e);
            }
        }
    }

    private void handleDefault(ErrorMessage msg, Throwable t)
    {
        ErrorMessage nestedMsg = null;
        // Try wrapping the exception and the Exception message that caused the
        // exception in a new message
        try
        {
            nestedMsg = new ErrorMessage(t);
        }
        catch (Exception e)
        {
            logger.fatal(LocaleMessage.defaultException(e), e);
            handleFatal(e);
        }
        try
        {
            defaultHandler.onException(nestedMsg);
        }
        catch (HandlerException e)
        {
            logger.fatal(LocaleMessage.defaultHandlerException(e), e);
            handleFatal(e);
        }

    }

    private void handleFatal(Throwable t)
    {
        // If this method has been called, all other handlers failed
        // this is all we can do
        logger.fatal(LocaleMessage.fatalException(t), t);
        ((MuleManager)MuleManager.getInstance()).shutdown(t, false);
    }
}
