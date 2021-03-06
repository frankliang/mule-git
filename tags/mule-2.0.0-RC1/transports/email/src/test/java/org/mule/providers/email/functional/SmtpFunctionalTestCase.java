/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.email.functional;

public class SmtpFunctionalTestCase extends AbstractEmailFunctionalTestCase
{

    public SmtpFunctionalTestCase()
    {
        super(65437, STRING_MESSAGE, "smtp");
    }

    public void testSend() throws Exception
    {
        doSend();
    }

}