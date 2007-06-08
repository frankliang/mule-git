/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.email.functional;

public class ImapFunctionalTestCase extends AbstractEmailFunctionalTestCase
{

    public ImapFunctionalTestCase()
    {
        super(65433, MIME_MESSAGE, "imap");
    }

    public void testReceive() throws Exception
    {
        doReceive();
    }

}