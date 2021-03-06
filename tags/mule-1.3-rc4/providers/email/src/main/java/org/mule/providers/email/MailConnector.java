/*
 * $Id$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.email;

import org.mule.umo.provider.UMOConnector;

import javax.mail.Authenticator;

/**
 * Implemented by  mail connectors to provide Mule with a Mail authenticator object
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public interface MailConnector extends UMOConnector
 {
    public Authenticator getAuthenticator();

    public int getDefaultPort();
}
