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
package org.mule.extras.spring.events;

import org.mule.umo.UMOManager;
import org.mule.MuleManager;

/**
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class SpringEventsWithEmbeddedManagerTestCase extends SpringEventsTestCase
{
    protected String getConfigResources() {
        return "mule-events-app-with-embedded-manager.xml";
    }

    public void testCorrectManagerLoaded() {
        assertNotNull(MuleManager.getInstance().getProperty("OS Version"));
    }
}
