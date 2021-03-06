/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.routing.filters.logic;

import org.mule.api.MuleMessage;
import org.mule.api.routing.filter.Filter;

import java.util.Iterator;

/**
 * <code>AndFilter</code> accepts only if the leftFilter and rightFilter filter
 * accept.
 */

public class AndFilter extends AbstractFilterCollection
{

    public AndFilter()
    {
        super();
    }

    /**
     *
     * @param left
     * @param right
     */
    public AndFilter(Filter left, Filter right)
    {
        super(left, right);
    }

    public boolean accept(MuleMessage message)
    {
        if(getFilters().size()==0)
        {
            return false;
        }
        int counter=0;
        for (Iterator iterator = getFilters().iterator(); iterator.hasNext();)
        {
            Filter umoFilter = (Filter) iterator.next();
            if(umoFilter.accept(message))
            {
                counter++;
            }
        }

        return counter == getFilters().size();
    }
}
