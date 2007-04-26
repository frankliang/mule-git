/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package net.sf.pzfilereader.writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom.JDOMException;

public class PzWriterFactory extends Object
{
    public static CsvWriter newCsvWriter(OutputStream output, char delimiter, char qualifier) 
        throws IOException
    {
        return new DefaultCsvWriter(output, delimiter, qualifier);
    }
    
    public static CsvWriter newCsvWriter(InputStream mapping, OutputStream output,
        char delimiter, char qualifier) throws IOException, JDOMException
    {
        return new MappedCsvWriter(mapping, output, delimiter, qualifier);
    }
}


