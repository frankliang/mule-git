/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.impl.model;

import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;

public abstract class AbstractComponentTestCase extends AbstractMuleTestCase
{

    protected UMOComponent component;

    public void testStart() throws UMOException
    {
        try
        {
            component.start();
            fail("Exception expected: Cannot start an uninitialised component");
        }
        catch (UMOException e)
        {
            // expected 
        }
        catch (NullPointerException npe)
        {
            // TODO MULE-2843
        }

        component.initialise();
        component.start();

        try
        {
            component.initialise();
            fail("Exception expected: Cannot initialise an already initialised component");
        }
        catch (InitialisationException e)
        {
            // expected
        }

    }

    public void testPause() throws UMOException
    {
        assertFalse(component.isStarted());
        assertFalse(component.isPaused());

        try
        {
            component.pause();
            fail("Exception expected: Cannot pause an uninitialised component");
        }
        catch (UMOException e)
        {
            // expected 
        }
        catch (NullPointerException npe)
        {
            // TODO MULE-2843
        }

        component.initialise();

        // Pausing a component that is not started does not throw an exception
        assertFalse(component.isStarted());
        assertFalse(component.isPaused());
        component.resume();
        assertFalse(component.isPaused());
        component.start();
        assertTrue(component.isStarted());
        assertFalse(component.isPaused());
        component.pause();
        assertTrue(component.isPaused());
        component.pause();
        assertTrue(component.isPaused());
    }

    public void testResume() throws UMOException
    {
        assertFalse(component.isStarted());
        assertFalse(component.isPaused());

        try
        {
            component.resume();
            fail("Exception expected: Cannot resume an uninitialised component");
        }
        catch (UMOException e)
        {
            // expected 
        }
        catch (NullPointerException npe)
        {
            // TODO MULE-2843
        }

        component.initialise();

        assertFalse(component.isStarted());
        assertFalse(component.isPaused());
        component.resume();
        assertFalse(component.isPaused());
        component.start();
        assertTrue(component.isStarted());
        assertFalse(component.isPaused());
        component.resume();
        assertFalse(component.isPaused());
        component.pause();
        assertTrue(component.isPaused());
        component.resume();
        assertFalse(component.isPaused());
        component.resume();
        assertFalse(component.isPaused());
    }

    public void testStop() throws UMOException
    {
        assertFalse(component.isStarted());
        assertFalse(component.isPaused());

        try
        {
            component.resume();
            fail("Exception expected: Cannot stop an uninitialised component");
        }
        catch (UMOException e)
        {
            // expected 
        }
        catch (NullPointerException npe)
        {
            // TODO MULE-2843
        }

        component.initialise();
        assertFalse(component.isStarted());

        component.stop();
        assertFalse(component.isStarted());
        component.start();
        assertTrue(component.isStarted());
        component.stop();
        assertFalse(component.isStarted());
        component.stop();
        assertFalse(component.isStarted());

    }

    public void testDispose() throws UMOException
    {
        assertFalse(component.isStarted());
        assertFalse(component.isPaused());

        try
        {
            component.dispose();
            fail("Exception expected: Cannot dispose an uninitialised component");
        }
        catch (Exception e)
        {
            // expected 
        }

        component.initialise();
        assertFalse(component.isStarted());

        component.dispose();
        assertFalse(component.isStarted());
        try
        {
            component.dispose();
            fail("Exception expected: Component has already been disposed");
        }
        catch (Exception e)
        {
            // expected 
        }
    }

}
