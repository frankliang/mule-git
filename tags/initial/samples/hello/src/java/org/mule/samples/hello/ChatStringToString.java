/* 
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk 
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */

package org.mule.samples.hello;

import org.mule.transformers.DefaultTransformer;
import org.mule.umo.transformer.TransformerException;

/**
 * <code>NameStringToChatString</code> is a dummy transform used in the hello world application
 * to transform the ChatString object into a string
 *
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class ChatStringToString extends DefaultTransformer
{
    /**
     *
     */
    public ChatStringToString()
    {
        super();
        this.registerSourceType(ChatString.class);
    }

    /* (non-Javadoc)
     * @see org.mule.transformers.AbstractTransformer#doTransform(java.lang.Object)
     */
    public Object doTransform(Object src) throws TransformerException
    {
        ChatString string = (ChatString) src;
        return string.toString();
    }
}
