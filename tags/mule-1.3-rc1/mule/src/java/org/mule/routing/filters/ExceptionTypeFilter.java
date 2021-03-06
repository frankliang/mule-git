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
package org.mule.routing.filters;

import org.mule.umo.UMOMessage;

/**
 * A filter that accepts messages that have an exception payload. An Exception
 * type can also be set on this filter to allow it to accept Exception messages
 * of a particular Exception class only.
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public class ExceptionTypeFilter extends PayloadTypeFilter
{
    public ExceptionTypeFilter() {
    }

    public ExceptionTypeFilter(Class expectedType) {
        super(expectedType);
    }

    /**
     * Check a given message against this filter.
     *
     * @param message a non null message to filter.
     * @return <code>true</code> if the message matches the filter
     */
    public boolean accept(UMOMessage message) {
        if(getExpectedType()==null)
        {
            return message.getExceptionPayload()!=null;
        } else if (message.getExceptionPayload()!=null){
            return getExpectedType().isAssignableFrom(message.getExceptionPayload().getException().getClass());
        } else {
            return false;
        }
    }
}
