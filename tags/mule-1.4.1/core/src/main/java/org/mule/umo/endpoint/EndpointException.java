/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.umo.endpoint;

import org.mule.config.i18n.Message;
import org.mule.umo.UMOException;

/**
 * <code>EndpointException</code> is an abstract exception extended by endpoint
 * specific exceptions.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */

public class EndpointException extends UMOException
{
    /**
     * Serial version
     */
    private static final long serialVersionUID = 3219403251233216800L;

    /**
     * @param message the exception message
     */
    public EndpointException(Message message)
    {
        super(message);
    }

    /**
     * @param message the exception message
     * @param cause the exception that cause this exception to be thrown
     */
    public EndpointException(Message message, Throwable cause)
    {
        super(message, cause);
    }

    public EndpointException(Throwable cause)
    {
        super(cause);
    }
}
