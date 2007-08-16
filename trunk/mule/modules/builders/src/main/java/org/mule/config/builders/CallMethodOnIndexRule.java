/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.builders;

import org.apache.commons.digester.CallMethodRule;

/**
 * TODO document
 */
public class CallMethodOnIndexRule extends CallMethodRule
{
    int index = 0;

    public CallMethodOnIndexRule(String s, int i, int index)
    {
        super(s, i);
        this.index = index;
    }

    public CallMethodOnIndexRule(String s, int index)
    {
        super(s);
        this.index = index;
    }

    public void end(String string, String string1) throws Exception
    {
        Object o = digester.peek(index);
        digester.push(o);
        super.end(string, string1);
        o = digester.pop();
    }

}
