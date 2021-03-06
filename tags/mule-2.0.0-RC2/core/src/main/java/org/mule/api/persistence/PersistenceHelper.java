/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.api.persistence;


/**
 * A PersistenceHelper is an object that can carry out some optional
 * pre-persistence steps for the Persistable. For example, the 
 * RegistryHelper represents an XStream Converter that prepares the XML
 * in a Spring format.
 */
public interface PersistenceHelper 
{
}


