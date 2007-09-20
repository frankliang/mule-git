/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.tcp.issues;

import org.mule.extras.client.MuleClient;
import org.mule.providers.tcp.protocols.SafeProtocolTestCase;
import org.mule.umo.UMOException;

public class SafeProtocolMule2227TestCase extends SafeProtocolTestCase
{

    // this actually "works" in that a response is received that looks reasonable.
    // that's just because the test is so simple that the length encoded string is read by the
    // server as a literal chunk of text (including the cookies and lengths!).  on the return these
    // are still present so the data that were sent are read (missing the appended text).
    public void testSafeToUnsafe() throws UMOException
    {
        MuleClient client = new MuleClient();
        assertResponseBad(client.send("tcp://localhost:65433?connector=safe", TEST_MESSAGE, null));
    }

}