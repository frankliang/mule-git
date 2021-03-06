/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.file.issues;

import org.mule.extras.client.MuleClient;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.providers.file.AbstractFileFunctionalTestCase;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.model.UMOModel;

import java.io.File;

/**
 * This used to be part of FileFunctionalTest; moved here to allow isolation of individual case.
 */
public class IndirectReceiveMule1842TestCase extends AbstractFileFunctionalTestCase
{

    public void testIndirectReceive() throws Exception
    {
        File target = initForReceive();

        // add a receiver endpoint that will poll the readFromDirectory
        UMOModel model = (UMOModel) managementContext.getRegistry().getModels().get("receiveModel");
        assertNotNull(model);
        UMOComponent relay = model.getComponent("relay");
        assertNotNull(relay);
        String url = fileToUrl(target) + "?connector=receiveConnector";
        logger.debug(url);
        relay.getDescriptor().getInboundRouter().addEndpoint(new MuleEndpoint(url, true));

        // then read from the queue that the polling receiver will write to
        MuleClient client = new MuleClient();
        UMOMessage message = client.receive("receive", 3000);
        checkReceivedMessage(message);
    }

}
