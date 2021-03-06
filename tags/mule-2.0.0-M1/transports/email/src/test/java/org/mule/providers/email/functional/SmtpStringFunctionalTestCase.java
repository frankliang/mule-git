/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.email.functional;

/**
 * No additional transformers
 */
public class SmtpStringFunctionalTestCase extends AbstractEmailFunctionalTestCase
{

    public SmtpStringFunctionalTestCase()
    {
        super(65447, STRING_MESSAGE, "smtp", "smtp-string-functional-test.xml");
    }

    public void testSend() throws Exception
    {
        doSend();
    }

}