/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.email;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.EndpointException;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.DispatchException;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.MessageFactory;
import org.mule.transport.AbstractMessageDispatcher;
import org.mule.transport.NullPayload;

import java.net.URLDecoder;
import java.util.Calendar;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

/**
 * <code>SmtpMessageDispatcher</code> will dispatch Mule events as Mime email
 * messages over an SMTP gateway.
 * 
 * This contains a reference to a transport (and endpoint and connector, via superclasses)
 */
public class SmtpMessageDispatcher extends AbstractMessageDispatcher
{
    private volatile Transport transport;

    public SmtpMessageDispatcher(OutboundEndpoint endpoint)
    {
        super(endpoint);
    }

    private SmtpConnector castConnector()
    {
        return (SmtpConnector) getConnector();
    }

    protected void doConnect() throws Exception
    {
        if (transport == null)
        {
            try
            {

                transport = castConnector().getSessionDetails(endpoint).newTransport();
                EndpointURI uri = endpoint.getEndpointURI();
                String encoding = endpoint.getEncoding();
                String user = (uri.getUser()!=null ? URLDecoder.decode(uri.getUser(), encoding) : null);
                String pass = (uri.getPassword()!=null ? URLDecoder.decode(uri.getPassword(), encoding) : null);
                transport.connect(uri.getHost(), uri.getPort(),  user, pass);
            }
            catch (Exception e)
            {
                throw new EndpointException(
                    MessageFactory.createStaticMessage("Unable to connect to mail transport."), e);
            }
        }
    }

    protected void doDisconnect() throws Exception
    {
        if (null != transport)
        {
            try
            {
                transport.close();
            }
            finally
            {
                transport = null;
            }
        }
    }

    protected void doDispatch(MuleEvent event) throws Exception
    {
        Object data = event.transformMessage();

        if (!(data instanceof Message))
        {
            throw new DispatchException(
                CoreMessages.transformUnexpectedType(data.getClass(), Message.class),
                event.getMessage(), event.getEndpoint());
        }
        else
        {
            // Check the message for any unset data and use defaults
            sendMailMessage((Message) data);
        }
    }

    protected MuleMessage doSend(MuleEvent event) throws Exception
    {
        doDispatch(event);
        return new DefaultMuleMessage(NullPayload.getInstance(), connector.getMuleContext());
    }

    protected void sendMailMessage(Message message) throws MessagingException
    {
        // sent date
        message.setSentDate(Calendar.getInstance().getTime());

        /*
         * Double check that the transport is still connected as some SMTP servers may 
         * disconnect idle connections.
         */
        if (!transport.isConnected())
        {
            EndpointURI uri = endpoint.getEndpointURI();
            if (logger.isInfoEnabled())
            {
                logger.info("Connection closed by remote server. Reconnecting.");
            }
            transport.connect(uri.getHost(), uri.getPort(), uri.getUser(), uri.getPassword());
        }

        transport.sendMessage(message, message.getAllRecipients());

        if (logger.isDebugEnabled())
        {
            StringBuffer msg = new StringBuffer();
            msg.append("Email message sent with subject'").append(message.getSubject()).append("' sent- ");
            msg.append(", From: ").append(MailUtils.mailAddressesToString(message.getFrom())).append(" ");
            msg.append(", To: ").append(
                MailUtils.mailAddressesToString(message.getRecipients(Message.RecipientType.TO))).append(" ");
            msg.append(", Cc: ").append(
                MailUtils.mailAddressesToString(message.getRecipients(Message.RecipientType.CC))).append(" ");
            msg.append(", Bcc: ")
            .append(MailUtils.mailAddressesToString(message.getRecipients(Message.RecipientType.BCC)))
            .append(" ");
            msg.append(", ReplyTo: ").append(MailUtils.mailAddressesToString(message.getReplyTo()));

            logger.debug(msg.toString());
        }

    }

    protected void doDispose()
    {
        // nothing doing
    }

}
