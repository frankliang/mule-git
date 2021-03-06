/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.vm.config;

import org.mule.config.QueueProfile;
import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.config.spring.parsers.specific.TransactionDefinitionParser;
import org.mule.endpoint.URIBuilder;
import org.mule.transport.vm.VMConnector;
import org.mule.transport.vm.VMTransactionFactory;

/**
 * Reigsters a Bean Definition Parser for handling <code><vm:connector></code> elements.
 *
 */
public class VmNamespaceHandler extends AbstractMuleNamespaceHandler
{

    public void init()
    {
        registerStandardTransportEndpoints(VMConnector.VM, URIBuilder.PATH_ATTRIBUTES);
        registerConnectorDefinitionParser(VMConnector.class);
        registerBeanDefinitionParser("queue-profile", new ChildDefinitionParser("queueProfile", QueueProfile.class));
        registerBeanDefinitionParser("transaction", new TransactionDefinitionParser(VMTransactionFactory.class));

        // DEPRECATED. Use "queue-profile"
        registerBeanDefinitionParser("queueProfile", new ChildDefinitionParser("queueProfile", QueueProfile.class));
    }

}
