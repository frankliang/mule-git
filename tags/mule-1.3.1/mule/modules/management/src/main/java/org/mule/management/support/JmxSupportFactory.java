/*
* $Id$
* --------------------------------------------------------------------------------------
* Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
*
* The software in this package is published under the terms of the BSD style
* license, a copy of which has been included with this distribution in the
* LICENSE.txt file.
*/
package org.mule.management.support;

/**
 * Factory for instantiating JMX helper classes.
 */
public interface JmxSupportFactory
{
    /**
     * Create an instance of a JMX support class.
     * @return class instance
     */
    JmxSupport newJmxSupport();
}
