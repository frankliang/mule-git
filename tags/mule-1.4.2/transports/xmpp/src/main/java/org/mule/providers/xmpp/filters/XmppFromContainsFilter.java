/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.xmpp.filters;

import org.jivesoftware.smack.filter.PacketFilter;

/**
 * <code>XmppFromContainsFilter</code> is an Xmpp FromContainsfilter adapter.
 */
public class XmppFromContainsFilter extends AbstractXmppFilter
{
    protected volatile String pattern;

    public XmppFromContainsFilter()
    {
        super();
    }

    public XmppFromContainsFilter(String pattern)
    {
        this.pattern = pattern;
    }

    public String getPattern()
    {
        return pattern;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    protected PacketFilter createFilter()
    {
        return new XmppFromContainsFilter(pattern);
    }
}
