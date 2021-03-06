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
package org.mule.test.integration.client;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class MuleClientRemotingHttpTestCase extends MuleClientRemotingTestCase
{
    protected String getConfigResources() {
        return "org/mule/test/integration/client/test-client-mule-config-remote-http.xml";
    }

    public String getServerUrl()
    {
        return "http://localhost:60504";
    }
}
