/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extras.jaas.config;

import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.extras.jaas.JaasSimpleAuthenticationProvider;
import org.mule.impl.security.PasswordBasedEncryptionStrategy;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class JaasNamespaceHandler extends NamespaceHandlerSupport
{
    public void init()
    {
        registerBeanDefinitionParser("jaas-security-provider", new ChildDefinitionParser("provider", JaasSimpleAuthenticationProvider.class));
        registerBeanDefinitionParser("password-encryption-strategy", new ChildDefinitionParser("encryptionStrategy", PasswordBasedEncryptionStrategy.class));
    }

}


