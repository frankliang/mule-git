/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.email.connectors;

import org.mule.providers.email.GreenMailUtilities;
import org.mule.tck.providers.AbstractConnectorTestCase;
import org.mule.umo.provider.UMOConnector;

import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.Servers;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Start a (greenmail) mail server with a known message, for use in subclasses.
 * Each test gets a new set of ports to avoid conflicts (shouldn't be needed, but
 * greenmail doesn't seem to be closing ports).  Also contains utility methods
 * for comparing emails, building endpoints, etc.
 */
public abstract class AbstractMailConnectorFunctionalTestCase extends AbstractConnectorTestCase
{

    // something odd happening here?  50006 seems to have failed a 
    // couple of times?
    public static final int INITIAL_SERVER_PORT = 50007;
    // large enough to jump away from a group of related ports
    public static final int PORT_INCREMENT = 17;
    // ie must succeed within RETRY_LIMIT attempts
    public static final int RETRY_LIMIT = 2;
    public static final String LOCALHOST = "127.0.0.1";
    public static final String USER = "bob";
    public static final String PROVIDER = "example.com";
    public static final String EMAIL = USER + "@" + PROVIDER;
    public static final String PASSWORD = "secret";
    public static final String MESSAGE = "Test Email Message";
    public static final long STARTUP_PERIOD_MS = 1000;

    // for constructor
    public static final boolean SEND_INITIAL_EMAIL = true;
    public static final boolean NO_INITIAL_EMAIL = false;

    private static final AtomicInteger nextPort = new AtomicInteger(INITIAL_SERVER_PORT);
    private static final Log staticLogger = LogFactory.getLog(AbstractMailConnectorFunctionalTestCase.class);
    private static final AtomicInteger nameCount = new AtomicInteger(0);

    private MimeMessage message;
    private Servers servers;
    private boolean initialEmail = false;
    
    protected AbstractMailConnectorFunctionalTestCase(boolean initialEmail)
    {
        super();
        this.initialEmail = initialEmail;
    }
    
    // @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        startServers();
    }
    
    // @Override
    protected void doTearDown() throws Exception 
    {
        stopServers();
        super.doTearDown();
    }

    private void storeEmail() throws Exception
    {
        GreenMailUtilities.storeEmail(servers.getManagers().getUserManager(),
                EMAIL, USER, PASSWORD, (MimeMessage) getValidMessage());
        assertEquals(1, servers.getReceivedMessages().length);
    }
    
    private void startServers() throws Exception
    {
        servers = new Servers(getSetups());
        servers.start();
        if (initialEmail)
        {
            storeEmail();
            // give greenmail time to stabilise(!?) (only needed for receive tests)
            Thread.sleep(STARTUP_PERIOD_MS);
        }
    }

    private static ServerSetup[] getSetups()
    {
        staticLogger.debug("generating new servers from: " + nextPort.get());
        ServerSetup smtp =
                new ServerSetup(nextPort.getAndAdd(PORT_INCREMENT), null, ServerSetup.PROTOCOL_SMTP);
        ServerSetup smtps =
                new ServerSetup(nextPort.getAndAdd(PORT_INCREMENT), null, ServerSetup.PROTOCOL_SMTPS);
        ServerSetup pop3 =
                new ServerSetup(nextPort.getAndAdd(PORT_INCREMENT), null, ServerSetup.PROTOCOL_POP3);
        ServerSetup pop3s =
                new ServerSetup(nextPort.getAndAdd(PORT_INCREMENT), null, ServerSetup.PROTOCOL_POP3S);
        ServerSetup imap =
                new ServerSetup(nextPort.getAndAdd(PORT_INCREMENT), null, ServerSetup.PROTOCOL_IMAP);
        ServerSetup imaps =
                new ServerSetup(nextPort.getAndAdd(PORT_INCREMENT), null, ServerSetup.PROTOCOL_IMAPS);
        return new ServerSetup[]{smtp, smtps, pop3, pop3s, imap, imaps};
    }

    private void stopServers() throws Exception
    {
        if (null != servers)
        {
            servers.stop();
        }
    }

    protected Servers getServers()
    {
        return servers;
    }

    // @Override
    public Object getValidMessage() throws Exception
    {
        if (null == message)
        {
            message = GreenMailUtilities.toMessage(MESSAGE, EMAIL);
        }
        return message;
    }
    
    protected String getPop3TestEndpointURI()
    {
        return buildEndpoint("pop3", servers.getPop3().getPort());
    }

    protected String getPop3sTestEndpointURI()
    {
        return buildEndpoint("pop3s", servers.getPop3s().getPort());
    }

    protected String getImapTestEndpointURI()
    {
        return buildEndpoint("imap", servers.getImap().getPort());
    }
    
    protected String getImapsTestEndpointURI()
    {
        return buildEndpoint("imaps", servers.getImaps().getPort());
    }
    
    protected String getSmtpTestEndpointURI()
    {
        return buildEndpoint("smtp", servers.getSmtp().getPort());
    }
    
    protected String getSmtpsTestEndpointURI()
    {
        return buildEndpoint("smtps", servers.getSmtps().getPort());
    }
    
   private String buildEndpoint(String protocol, int port) 
    {
        return protocol + "://" + USER + ":" + PASSWORD + "@" + LOCALHOST + ":" + port +
        "?connector=" + connectorName;
    }
    
    protected void assertMessageOk(Object message) throws Exception
    {
        assertTrue("Did not receive a MimeMessage", message instanceof MimeMessage);
        MimeMessage received = (MimeMessage) message;
        // for some reason, something is adding a newline at the end of messages
        // so we need to strip that out for comparison
        assertTrue("Did not receive a message with String contents",
            received.getContent() instanceof String);
        String receivedText = ((String) received.getContent()).trim();
        assertEquals(MESSAGE, receivedText);
        assertNotNull(received.getRecipients(Message.RecipientType.TO));
        assertEquals(1, received.getRecipients(Message.RecipientType.TO).length);
        assertEquals(received.getRecipients(Message.RecipientType.TO)[0].toString(), EMAIL);
    }

    protected String uniqueName(String root)
    {
        return root + nameCount.incrementAndGet();
    }

}
