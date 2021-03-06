/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.impl.internal.notifications.manager;

import org.mule.umo.manager.UMOServerNotificationListener;

/**
 * A simple tuple that stores a listener with an optional subscription (used to match a resource ID).
 */
public class ListenerSubscriptionPair
{

    private UMOServerNotificationListener listener;
    private String subscription = ServerNotificationManager.NULL_SUBSCRIPTION;
    private boolean nullSubscription = true;

    /**
     * For config - must be constructed using the setters
     */
    public ListenerSubscriptionPair()
    {
        // empty
    }

    public ListenerSubscriptionPair(UMOServerNotificationListener listener)
    {
        setListener(listener);
    }

    public ListenerSubscriptionPair(UMOServerNotificationListener listener, String subscription)
    {
        setListener(listener);
        setSubscription(subscription);
    }

    public void setListener(UMOServerNotificationListener listener)
    {
        this.listener = listener;
    }

    public void setSubscription(String subscription)
    {
        if (null != subscription)
        {
            this.subscription = subscription;
            nullSubscription = false;
        }
    }

    public UMOServerNotificationListener getListener()
    {
        return listener;
    }

    public String getSubscription()
    {
        return subscription;
    }

    public boolean isNullSubscription()
    {
        return nullSubscription;
    }

//    public int hashCode()
//    {
//        return (null == listener ? 1 : listener.hashCode())
//                ^ (null == subscription ? 2 : subscription.hashCode())
//                ^ Boolean.valueOf(isNullSubscription()).hashCode();
//    }
//
//    public boolean equals(Object obj)
//    {
//        if (obj instanceof ListenerSubscriptionPair)
//        {
//            ListenerSubscriptionPair other = (ListenerSubscriptionPair) obj;
//            return other.listener.equals(listener)
//                    && other.subscription.equals(subscription)
//                    && other.nullSubscription == nullSubscription;
//        }
//        else
//        {
//            return false;
//        }
//    }

}
