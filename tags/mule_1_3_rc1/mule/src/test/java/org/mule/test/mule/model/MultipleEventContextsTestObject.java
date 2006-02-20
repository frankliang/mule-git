/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 */

package org.mule.test.mule.model;

import org.mule.umo.UMOEventContext;

/**
 * A test object with multiple matching methods accepting
 * UMOEventContext for the discovery to fail..
 *
 * @author <a href="mailto:aperepel@itci.com">Andrew Perepelytsya</a>
 */
public class MultipleEventContextsTestObject
{
    public void onCall(UMOEventContext context) {

    }

    public void doCall(UMOEventContext context) {

    }
}
