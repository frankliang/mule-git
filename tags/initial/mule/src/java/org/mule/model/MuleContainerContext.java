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

package org.mule.model;

import org.mule.umo.model.ComponentNotFoundException;
import org.mule.umo.model.UMOContainerContext;
import org.mule.util.ClassHelper;

/**
 * <code>MuleContainerContext</code> is a default resolver that doesn't support external reference resolution.
 * It's function is to provide a complete implementation when a componenet resolver is not defined. The default (legacy)
 * behaviour is to build a component key as a fully qualified class name
 *
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class MuleContainerContext implements UMOContainerContext
{

    /* (non-Javadoc)
     * @see org.mule.model.UMOContainerContext#getComponent(java.lang.Object)
     */
    public Object getComponent(Object key) throws ComponentNotFoundException
    {
        if (key == null)
        {
            throw new ComponentNotFoundException("Component not found for null key");
        }
        try
        {
            Class clazz;
            if (key instanceof Class)
            {
                clazz = (Class) key;
            }
            else
            {
                clazz = ClassHelper.loadClass(key.toString(), getClass());
            }
            return clazz.newInstance();
        }
        catch (Exception e)
        {
            throw new ComponentNotFoundException("Failed to instanciate: " + key.toString() + ". Exception is: " + e.getMessage(), e);
        }
    }

}
