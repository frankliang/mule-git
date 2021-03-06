/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.boot;

import java.io.File;

public class GuiInstallerLibraryDownloader
{
    public static void main(String args[]) throws Exception
    {
        File muleHome = new File(args[0]);
        MuleBootstrapUtils.addLocalJarFilesToClasspath(muleHome, muleHome);
    }
}
