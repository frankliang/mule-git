/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.mule.security;

import org.mule.security.SecretKeyEncryptionStrategy;
import org.mule.security.SecretKeyFactory;
import org.mule.tck.AbstractMuleTestCase;

public class SecretKeyEncryptionStrategyTestCase extends AbstractMuleTestCase
{

    public void testRoundTripEncryptionBlowfish() throws Exception
    {
        SecretKeyEncryptionStrategy ske = new SecretKeyEncryptionStrategy();
        ske.setAlgorithm("Blowfish");
        ske.setKey("shhhhh");
        ske.initialise();

        byte[] b = ske.encrypt("hello".getBytes(), null);

        assertNotSame(new String(b), "hello");
        String s = new String(ske.decrypt(b, null), "UTF-8");
        assertEquals("hello", s);
    }

    public void testRoundTripEncryptionBlowfishWithKeyFactory() throws Exception
    {
        SecretKeyEncryptionStrategy ske = new SecretKeyEncryptionStrategy();
        ske.setAlgorithm("Blowfish");
        ske.setKeyFactory(new SecretKeyFactory()
        {
            public byte[] getKey()
            {
                return "shhhh".getBytes();
            }
        });
        ske.initialise();

        byte[] b = ske.encrypt("hello".getBytes(), null);

        assertNotSame(new String(b), "hello");
        String s = new String(ske.decrypt(b, null), "UTF-8");
        assertEquals("hello", s);
    }

    public void testRoundTripEncryptionTripleDES() throws Exception
    {
        SecretKeyEncryptionStrategy ske = new SecretKeyEncryptionStrategy();
        ske.setAlgorithm("TripleDES");
        ske.setKey("shhhhh");

        ske.initialise();

        byte[] b = ske.encrypt("hello".getBytes(), null);

        assertNotSame(new String(b), "hello");
        String s = new String(ske.decrypt(b, null), "UTF-8");
        assertEquals("hello", s);
    }

}
