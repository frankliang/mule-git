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

import org.mule.config.builders.QuickConfigurationBuilder;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transformers.NoActionTransformer;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;

public class EndpointTransformerTestCase extends AbstractMuleTestCase
{

    public void testTransformerProperty() throws UMOException
    {
        QuickConfigurationBuilder builder = new QuickConfigurationBuilder();
        builder.getManager().registerTransformer(new NoActionTransformer());
        UMOEndpoint endpoint = builder.registerEndpoint("test:///tmp?transformers=NoActionTransformer",
            "test", false);
        assertEquals("NoActionTransformer", endpoint.getTransformer().getName());
    }

    public void testResponseTransformerProperty() throws UMOException
    {
        QuickConfigurationBuilder builder = new QuickConfigurationBuilder();
        builder.getManager().registerTransformer(new NoActionTransformer());
        UMOEndpoint endpoint = builder.registerEndpoint(
            "test:///tmp?responseTransformers=NoActionTransformer", "test", false);
        assertEquals("NoActionTransformer", endpoint.getResponseTransformer().getName());
    }
}
