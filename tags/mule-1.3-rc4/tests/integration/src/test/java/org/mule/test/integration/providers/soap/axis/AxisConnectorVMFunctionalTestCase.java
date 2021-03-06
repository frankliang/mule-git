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
package org.mule.test.integration.providers.soap.axis;

import org.mule.test.integration.providers.soap.AbstractSoapResourceEndpointFunctionalTestCase;


/**
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class AxisConnectorVMFunctionalTestCase extends AbstractSoapResourceEndpointFunctionalTestCase
 {
    protected String getTransportProtocol() {
        return "vm";
    }

    protected String getSoapProvider() {
        return "axis";
    }
      
    public String getConfigResources() {
        return "org/mule/test/integration/providers/soap/axis/axis-" + getTransportProtocol() + "-mule-config.xml";
    }
}
