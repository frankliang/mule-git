/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jms.i18n;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;
import org.mule.util.ClassUtils;
import org.mule.util.StringMessageUtils;

public class JmsMessages extends MessageFactory
{
    private static final String BUNDLE_PATH = getBundlePath("jms");
    
    public static Message connectorDoesNotSupportSyncReceiveWhenTransacted()
    {
        return createMessage(BUNDLE_PATH, 2);
    }

    public static Message invalidResourceType(Class expectedClass, Class actualClass)
    {
        return createMessage(BUNDLE_PATH, 12, StringMessageUtils.toString(expectedClass),
            StringMessageUtils.toString(actualClass));
    }

    public static Message checkTransformer(String string, Class class1, String name)
    {
        return createMessage(BUNDLE_PATH, 14, string, ClassUtils.getSimpleName(class1.getClass()),
            name);
    }
}


