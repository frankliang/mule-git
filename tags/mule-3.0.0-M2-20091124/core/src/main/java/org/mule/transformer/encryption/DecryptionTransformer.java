/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformer.encryption;

import org.mule.api.security.CryptoFailureException;

/**
 * <code>EncryptionTransformer</code> will transform an encrypted array of bytes or
 * string into an decrypted array of bytes
 */
public class DecryptionTransformer extends AbstractEncryptionTransformer
{

    protected byte[] getTransformedBytes(byte[] buffer) throws CryptoFailureException
    {
        return getStrategy().decrypt(buffer, null);
    }

}
