/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.ra;

import org.mule.config.i18n.CoreMessages;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

/**
 * <code>RaHelper</code> is a collection of helper methods used by this RA
 * implementation
 */
// @ThreadSafe
public class RaHelper
{
    public static PasswordCredential getPasswordCredential(final ManagedConnectionFactory mcf,
                                                           final Subject subject,
                                                           ConnectionRequestInfo info)
        throws ResourceException
    {
        if (subject == null)
        {
            if (info == null)
            {
                return null;
            }
            else
            {
                MuleConnectionRequestInfo muleInfo = (MuleConnectionRequestInfo)info;

                // Can't create a PC with null values
                if (muleInfo.getUserName() == null || muleInfo.getPassword() == null)
                {
                    // logger.info("\tUtil::GetPasswordCred: User or password is
                    // null");
                    return null;
                }

                char[] password = muleInfo.getPassword().toCharArray();
                PasswordCredential pc = new PasswordCredential(muleInfo.getUserName(), password);
                pc.setManagedConnectionFactory(mcf);
                return pc;
            }
        }
        else
        {
            PasswordCredential pc = (PasswordCredential)AccessController.doPrivileged(new PrivilegedAction()
            {
                public Object run()
                {
                    Set creds = subject.getPrivateCredentials(PasswordCredential.class);
                    Iterator iter = creds.iterator();
                    while (iter.hasNext())
                    {
                        PasswordCredential candidate = (PasswordCredential)iter.next();
                        if (candidate != null)
                        {
                            ManagedConnectionFactory candidatemcf = candidate.getManagedConnectionFactory();
                            if (candidatemcf != null && candidatemcf.equals(mcf))
                            {
                                return candidate;
                            }
                        }
                    }
                    return null;
                }
            });
            if (pc == null)
            {
                throw new java.lang.SecurityException(CoreMessages.authNoCredentials().getMessage());
            }
            else
            {
                return pc;
            }
        }
    }
}
