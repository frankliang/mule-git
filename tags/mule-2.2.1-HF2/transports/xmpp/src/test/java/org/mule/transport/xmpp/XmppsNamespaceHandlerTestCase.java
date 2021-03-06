/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.xmpp;

import org.mule.transport.xmpp.XmppsConnector;

public class XmppsNamespaceHandlerTestCase extends AbstractNamespaceHandlerTestCase
{

    public XmppsNamespaceHandlerTestCase()
    {
        super(XmppsConnector.XMPPS);
    }

    public void testSubclassConfig() throws Exception
    {
        XmppsConnector connector = (XmppsConnector) muleContext.getRegistry().lookupConnector(
            "xmppsConnector");
        assertNotNull(connector);
    }

}
