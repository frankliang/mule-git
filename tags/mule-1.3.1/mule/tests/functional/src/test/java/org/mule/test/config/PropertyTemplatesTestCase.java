/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.config;

import org.mule.MuleManager;
import org.mule.providers.AbstractConnector;
import org.mule.providers.SimpleRetryConnectionStrategy;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMODescriptor;
import org.mule.umo.endpoint.UMOEndpoint;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class PropertyTemplatesTestCase extends FunctionalTestCase
{

    protected String getConfigResources()
    {
        return "test-xml-property-templates.xml";
    }

    public void testProperties()
    {
        assertEquals("blah", MuleManager.getConfiguration().getModel());
        assertEquals("blah", MuleManager.getInstance().getModel().getName());

        AbstractConnector c = (AbstractConnector)MuleManager.getInstance().lookupConnector("myTestConnector");
        assertNotNull(c);
        assertTrue(c.getConnectionStrategy() instanceof SimpleRetryConnectionStrategy);
        assertEquals(4, ((SimpleRetryConnectionStrategy)c.getConnectionStrategy()).getRetryCount());

        UMODescriptor d = MuleManager.getInstance().getModel().getDescriptor("test-from-env-props");
        assertNotNull(d);
        assertEquals(d.getInboundEndpoint().getEndpointURI().toString(), "test://test.1");
        assertEquals(d.getOutboundEndpoint().getEndpointURI().toString(), "test://test.2");
    }

    public void testPropertyWithoutOverrides()
    {

        UMODescriptor d = MuleManager.getInstance().getModel().getDescriptor("test2");
        assertNotNull(d);
        UMOEndpoint endpoint = d.getInboundRouter().getEndpoint("ep1");
        assertNotNull(endpoint);
        assertNotNull(endpoint.getProperties());
        assertEquals(3, endpoint.getProperties().size());
        assertEquals("value1", endpoint.getProperties().get("prop1"));
        assertEquals("overrideValue2", endpoint.getProperties().get("prop2"));
        assertEquals("value3", endpoint.getProperties().get("prop3"));
    }

    public void testPropertyWithOverrides()
    {

        UMODescriptor d = MuleManager.getInstance().getModel().getDescriptor("test3");
        assertNotNull(d);
        UMOEndpoint endpoint = d.getInboundRouter().getEndpoint("ep2");
        assertNotNull(endpoint);
        assertEquals("test://embedded", endpoint.getEndpointURI().getUri().toString());
        assertNotNull(endpoint.getProperties());
        assertEquals(3, endpoint.getProperties().size());
        assertEquals("value1", endpoint.getProperties().get("prop1"));
        assertEquals("value2", endpoint.getProperties().get("prop2"));
        assertEquals("value3", endpoint.getProperties().get("prop3"));
    }

    public void testPropertyMapsListsEtc()
    {

        UMODescriptor d = MuleManager.getInstance().getModel().getDescriptor("test3");
        assertNotNull(d);
        Map props = (Map)d.getProperties().get("testMap");
        assertNotNull(props);
        assertEquals("foo entry", props.get("foo"));

        List list = (List)d.getProperties().get("testList");
        assertNotNull(list);
        assertEquals("bar entry", list.get(0).toString());

        assertEquals("${this should not be found}", d.getProperties().get("blah"));
        assertEquals("two tags foo entry and bar entry.", d.getProperties().get("foobar"));
    }
}
