/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.util;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.util.DateUtils;

import java.util.Date;

public class DateUtilsTestCase extends AbstractMuleTestCase
{
    private final String TEST_DATE_FORMAT = "dd/MM/yyyy hh:mm:ss";
    private final String TEST_DATE_FORMAT_2 = "dd-MM-yy, hh:mm";

    public void testDateUtils() throws Exception
    {
        String date = "12/11/2002 12:06:47";

        Date result = DateUtils.getDateFromString(date, TEST_DATE_FORMAT);
        assertTrue(result.before(new Date(System.currentTimeMillis())));

        String newDate = DateUtils.getStringFromDate(result, TEST_DATE_FORMAT);
        assertEquals(date, newDate);

        String timestamp = DateUtils.formatTimeStamp(result, TEST_DATE_FORMAT_2);
        assertEquals("12-11-02, 12:06", timestamp);

        String newTimestamp = DateUtils.getTimeStamp(TEST_DATE_FORMAT_2);
        assertEquals(DateUtils.getStringFromDate(new Date(), TEST_DATE_FORMAT_2), newTimestamp);
    }

}
