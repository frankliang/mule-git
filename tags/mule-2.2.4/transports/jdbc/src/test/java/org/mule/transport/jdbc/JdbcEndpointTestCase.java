/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jdbc;

import org.mule.api.endpoint.EndpointURI;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;

public class JdbcEndpointTestCase extends AbstractMuleTestCase
{

    public void testWithoutEndpointName() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("jdbc:/?sql=SELECT * FROM TABLE");
        url.initialise();
        assertEquals("jdbc", url.getScheme());
        assertEquals("", url.getAddress());
        assertNull(url.getEndpointName());
        assertNotNull(url.getParams());
        assertEquals("SELECT * FROM TABLE", url.getParams().get("sql"));
        assertEquals("jdbc:/?sql=SELECT%20*%20FROM%20TABLE", url.toString());
    }

    public void testWithoutEndpointName2() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("jdbc://?sql=SELECT * FROM TABLE");
        url.initialise();
        assertEquals("jdbc", url.getScheme());
        assertEquals("jdbc", url.getAddress());
        assertNull(url.getEndpointName());
        assertNotNull(url.getParams());
        assertEquals("SELECT * FROM TABLE", url.getParams().get("sql"));
        assertEquals("jdbc://?sql=SELECT%20*%20FROM%20TABLE", url.toString());
    }

    public void testWithEndpointName() throws Exception
    {
        EndpointURI url = new MuleEndpointURI("jdbc://history/writeTests?type=2");
        url.initialise();
        assertEquals("jdbc", url.getScheme());
        assertEquals("writeTests", url.getAddress());
        assertEquals("history", url.getEndpointName());
        assertNotNull(url.getParams());
        assertEquals("2", url.getParams().get("type"));
        assertEquals("jdbc://history/writeTests?type=2", url.toString());
    }

}
