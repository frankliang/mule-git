/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.test.routing.outbound;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import org.mule.impl.MuleMessage;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.routing.outbound.AbstractMessageSplitter;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOSession;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */

public class MessageSplitterRouterTestCase extends AbstractMuleTestCase
{
    public void testMessageSpltterRouter() throws Exception
    {
        Mock session = getMockSession();

        UMOEndpoint endpoint1 = getTestEndpoint("Test1Provider", UMOEndpoint.ENDPOINT_TYPE_SENDER);
        endpoint1.setEndpointURI(new MuleEndpointURI("test://endpointUri.1"));
        UMOEndpoint endpoint2 = getTestEndpoint("Test2Provider", UMOEndpoint.ENDPOINT_TYPE_SENDER);
        endpoint2.setEndpointURI(new MuleEndpointURI("test://endpointUri.2"));
        UMOEndpoint endpoint3 = getTestEndpoint("Test3Provider", UMOEndpoint.ENDPOINT_TYPE_SENDER);
        endpoint3.setEndpointURI(new MuleEndpointURI("test://endpointUri.3"));

        //Dummy message splitter
        AbstractMessageSplitter router = new AbstractMessageSplitter() {
            private List parts;
            protected void initialise(UMOMessage message)
            {
                multimatch = false;
                parts = Arrays.asList(Utility.split(message.getPayload().toString(), ","));
            }

            protected UMOMessage getMessagePart(UMOMessage message, UMOEndpoint endpoint)
            {
                if(endpoint.getEndpointURI().getAddress().equals("endpointUri.1")) {
                    return new MuleMessage(parts.get(0), null);
                } else if(endpoint.getEndpointURI().getAddress().equals("endpointUri.2")) {
                    return new MuleMessage(parts.get(1), null);
                } else if(endpoint.getEndpointURI().getAddress().equals("endpointUri.3")) {
                    return new MuleMessage(parts.get(2), null);
                } else {
                    return null;
                }
            }
        };

        List endpoints = new ArrayList();
        endpoints.add(endpoint1);
        endpoints.add(endpoint2);
        endpoints.add(endpoint3);
        router.setEndpoints(endpoints);

        UMOMessage message = new MuleMessage("test,mule,message", null);

        assertTrue(router.isMatch(message));
        session.expect("dispatchEvent", C.args(C.isA(UMOMessage.class), C.eq(endpoint1)));
        session.expect("dispatchEvent", C.args(C.isA(UMOMessage.class), C.eq(endpoint2)));
        session.expect("dispatchEvent", C.args(C.isA(UMOMessage.class), C.eq(endpoint3)));
        router.route(message, (UMOSession)session.proxy(), false);
        session.verify();

        message = new MuleMessage("test,mule,message", null);

        session.expectAndReturn("sendEvent", C.args(C.isA(UMOMessage.class), C.eq(endpoint1)), message);
        session.expectAndReturn("sendEvent", C.args(C.isA(UMOMessage.class), C.eq(endpoint2)), message);
        session.expectAndReturn("sendEvent", C.args(C.isA(UMOMessage.class), C.eq(endpoint3)), message);
        UMOMessage result = router.route(message, (UMOSession)session.proxy(), true);
        assertNotNull(result);
        assertEquals(message, result);
        session.verify();
    }
}
