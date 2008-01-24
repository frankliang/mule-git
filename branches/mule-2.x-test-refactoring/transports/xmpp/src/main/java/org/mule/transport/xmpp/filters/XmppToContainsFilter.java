/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.xmpp.filters;

import org.jivesoftware.smack.filter.PacketFilter;

/**
 * <code>XmppToContainsFilter</code> is an Xmpp ToContainsfilter adapter.
 */
public class XmppToContainsFilter extends XmppFromContainsFilter
{
    public XmppToContainsFilter()
    {
        super();
    }

    public XmppToContainsFilter(String expression)
    {
        super(expression);
    }

    protected PacketFilter createFilter()
    {
        return new org.jivesoftware.smack.filter.ToContainsFilter(pattern);
    }
}
