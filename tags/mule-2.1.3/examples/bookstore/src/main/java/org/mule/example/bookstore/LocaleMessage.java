/*
 * $$Id$$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.example.bookstore;

import org.mule.config.i18n.MessageFactory;

public class LocaleMessage extends MessageFactory
{
    private static final LocaleMessage factory = new LocaleMessage();
    
    private static final String BUNDLE_PATH = "messages.bookstore-messages";
    
    public static String getWelcomeMessage()
    {
        return factory.getString(BUNDLE_PATH, 1);
    }

    public static String getMenuOption1()
    {
        return factory.getString(BUNDLE_PATH, 2);
    }

    public static String getMenuOption2()
    {
        return factory.getString(BUNDLE_PATH, 3);
    }
    
    public static String getMenuOption3()
    {
        return factory.getString(BUNDLE_PATH, 4);
    }
    
    public static String getMenuOption4()
    {
        return factory.getString(BUNDLE_PATH, 5);
    }
    
    public static String getMenuOptionQuit()
    {
        return factory.getString(BUNDLE_PATH, 6);
    }

    public static String getMenuPromptMessage()
    {
        return factory.getString(BUNDLE_PATH, 7);
    }

    public static String getGoodbyeMessage()
    {
        return factory.getString(BUNDLE_PATH, 8);
    }

    public static String getMenuErrorMessage()
    {
        return factory.getString(BUNDLE_PATH, 9);
    }
    
    public static String getBookTitlePrompt()
    {
        return factory.getString(BUNDLE_PATH, 10);
    }
    
    public static String getAuthorNamePrompt()
    {
        return factory.getString(BUNDLE_PATH, 11);
    }
    
    public static String getAddBooksMessagePrompt()
    {
        return factory.getString(BUNDLE_PATH, 12);
    }
    
    public static String getOrderWelcomeMessage()
    {
        return factory.getString(BUNDLE_PATH, 13);
    }
    
    public static String getBookIdPrompt()
    {
        return factory.getString(BUNDLE_PATH, 14);
    }
    
    public static String getHomeAddressPrompt()
    {
        return factory.getString(BUNDLE_PATH, 15);
    }
    
    public static String getEmailAddressPrompt()
    {
        return factory.getString(BUNDLE_PATH, 16);
    }
}
