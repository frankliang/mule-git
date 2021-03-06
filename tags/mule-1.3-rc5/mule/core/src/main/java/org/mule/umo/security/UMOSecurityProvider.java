/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.umo.security;

import org.mule.umo.lifecycle.Initialisable;

/**
 * <code>UMOSecurityProvider</code> is a target security provider thsat
 * actually does the work of authenticating credentials and populating the
 * UMOAuthentication object.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */

public interface UMOSecurityProvider extends Initialisable
{
    void setName(String name);

    String getName();

    UMOAuthentication authenticate(UMOAuthentication authentication) throws SecurityException;

    boolean supports(Class aClass);

    UMOSecurityContext createSecurityContext(UMOAuthentication auth) throws UnknownAuthenticationTypeException;
}
