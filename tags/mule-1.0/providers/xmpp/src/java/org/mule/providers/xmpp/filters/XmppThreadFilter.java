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
 */
package org.mule.providers.xmpp.filters;

import org.jivesoftware.smack.filter.PacketFilter;
/**
 * <code>XmppThreadFilter</code> is an Xmpp ThreadFilter
 * adapter.
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class XmppThreadFilter extends XmppFromContainsFilter
{
    public XmppThreadFilter() {
    }

    public XmppThreadFilter(String expression) {
        super(expression);
    }

    protected PacketFilter createFilter() {
        return new org.jivesoftware.smack.filter.ThreadFilter(pattern);
    }
}
