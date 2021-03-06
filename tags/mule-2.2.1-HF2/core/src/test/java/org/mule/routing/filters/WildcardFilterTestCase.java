/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.routing.filters;

import org.mule.tck.AbstractMuleTestCase;

public class WildcardFilterTestCase extends AbstractMuleTestCase
{

    public void testWildcardFilterNoPattern()
    {
        // start with default
        WildcardFilter filter = new WildcardFilter();
        assertFalse(filter.accept("No tengo dinero"));

        // activate a pattern
        filter.setPattern("* brown fox");
        assertTrue(filter.accept("The quick brown fox"));

        // remove pattern again, i.e. block all
        filter.setPattern(null);
        assertFalse(filter.accept("oh-oh"));
    }

    public void testWildcardFilterPostfix()
    {
        WildcardFilter filter = new WildcardFilter("The quick *");
        assertNotNull(filter.getPattern());
        assertTrue(filter.accept("The quick brown fox"));
        assertTrue(filter.accept("The quick *"));

        assertTrue(!filter.accept("The quickbrown fox"));
        assertTrue(!filter.accept("he quick brown fox"));
    }

    public void testWildcardFilterPrefix()
    {
        WildcardFilter filter = new WildcardFilter();
        filter.setPattern("* brown fox");
        assertTrue(filter.accept("The quick brown fox"));
        assertTrue(filter.accept("* brown fox"));

        assertTrue(!filter.accept("The quickbrown fox"));
        assertTrue(!filter.accept("The quick brown fo"));

    }

    public void testWildcardFilterPrePost()
    {
        WildcardFilter filter = new WildcardFilter();
        filter.setPattern("* brown *");
        assertTrue(filter.accept("The quick brown fox"));
        assertTrue(filter.accept("* brown fox"));

        assertTrue(!filter.accept("The quickbrown fox"));
        assertTrue(filter.accept("The quick brown fo"));

        filter.setPattern("**");
        assertTrue(filter.accept("The quick brown fox"));

        filter.setPattern("*w*");
        assertTrue(filter.accept("The quick brown fox"));

        filter.setPattern("*");
        assertTrue(filter.accept("The quick brown fox"));

        filter.setPattern("*.*");
        assertTrue(filter.accept("test.xml"));

        filter.setPattern("*.txt");
        assertTrue(filter.accept("test.txt"));
    }

    public void testWildcardFilterMultiplePatterns()
    {
        WildcardFilter filter = new WildcardFilter();
        filter.setPattern("* brown*, The*");
        assertTrue(filter.accept("The quick brown fox"));
        assertTrue(filter.accept(" brown fox"));

        assertTrue(filter.accept("The quickbrown fox"));

        filter.setPattern("* if, The*");
        assertTrue(!filter.accept("What The!"));
        assertTrue(!filter.accept("simplify"));

    }

    public void testWildcardFilterCasesensitive()
    {
        WildcardFilter filter = new WildcardFilter();
        filter.setPattern("* brown fox");
        assertFalse(filter.accept("The quick Brown fox"));
        assertTrue(filter.accept("* brown fox"));
        filter.setCaseSensitive(false);
        assertTrue(filter.accept("The quick Brown fox"));
    }

}
