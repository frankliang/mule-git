/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.registry;

/**
 * TODO document
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision$
 */
public interface RegistryDescriptor
{

    RegistryComponent getComponent();

    boolean isServiceAssembly();

    boolean isSharedLibrary();

    boolean isComponent();

    boolean isServices();

    Object getConfiguration();

    void validate() throws ValidationException;
}
