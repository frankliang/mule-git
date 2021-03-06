/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.soap.axis.transport.pop3;

import java.net.URL;
import java.net.URLConnection;

import org.mule.providers.soap.axis.transport.VoidURLConnection;

/**
 * A Dummy Url handler for handling pop3. This is needed becuase Axis uses
 * urlStreamHandlers to parse non-http urls.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class Handler extends java.net.URLStreamHandler
{
    protected URLConnection openConnection(URL url)
    {
        return new VoidURLConnection(url);
    }
}
