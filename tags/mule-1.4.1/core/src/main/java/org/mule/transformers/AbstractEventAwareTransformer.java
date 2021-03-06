/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformers;

import org.mule.config.i18n.CoreMessages;
import org.mule.impl.RequestContext;
import org.mule.umo.UMOEventContext;
import org.mule.umo.transformer.TransformerException;

/**
 * <code>AbstractEventAwareTransformer</code> is a transformer that has a reference
 * to the current message. This message can be used obtains properties associated
 * with the current message useful to the transform. Note that when part of a
 * transform chain, the Message payload reflects the pre-transform message state,
 * unless there is no current event for this thread, then the message will be a new
 * MuleMessage with the src as its payload. Transformers should always work on the
 * src object not the message payload.
 * 
 * @see org.mule.umo.UMOMessage
 * @see org.mule.impl.MuleMessage
 */

public abstract class AbstractEventAwareTransformer extends AbstractTransformer
{
    public final Object doTransform(Object src, String encoding) throws TransformerException
    {
        UMOEventContext event = RequestContext.getEventContext();
        if (event == null && requiresCurrentEvent())
        {
            throw new TransformerException(CoreMessages.noCurrentEventForTransformer(), this);
        }
        return transform(src, encoding, event);
    }

    public abstract Object transform(Object src, String encoding, UMOEventContext context)
        throws TransformerException;

    protected boolean requiresCurrentEvent()
    {
        return true;
    }
}
