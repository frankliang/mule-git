/* 
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */

package org.mule.tck.testmodels.fruit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.Disposable;
import org.mule.umo.lifecycle.Startable;
import org.mule.umo.lifecycle.Stoppable;

import java.util.HashMap;

public class WaterMelon implements Fruit, Startable, Stoppable, Disposable
{
    /**
     * logger used by this class
     */
    private static transient Log logger = LogFactory.getLog(WaterMelon.class);

    private boolean bitten = false;
    private Integer seeds = new Integer(100);
    private Double radius = new Double(4.34);
    private String brand;
    private String state = "void";

    public WaterMelon()
    {
    }

    public WaterMelon(HashMap props) throws UMOException
    {
        logger.info("Initialisaing Water melon with hashmap constructor");
        setBrand((String) props.get("namespace.brand"));
        setRadius((Double) props.get("another.namespace.radius"));
        setSeeds((Integer) props.get("seeds"));
        state = "initialised";
    }

    // public WaterMelon(String brand, Double radius, Integer seeds)
    // {
    // this.brand = brand;
    // this.radius = radius;
    // this.seeds = seeds;
    // }

    public void bite()
    {
        bitten = true;
    }

    public boolean isBitten()
    {
        return bitten;
    }

    public void myEventHandler(UMOEvent event) throws UMOException
    {
        logger.debug("Water Melon received an event in MyEventHandler! Event says: " + event.getMessageAsString());
        bite();
    }

    /**
     * @return
     */
    public String getBrand()
    {
        return brand;
    }

    /**
     * @return
     */
    public Integer getSeeds()
    {
        return seeds;
    }

    /**
     * @return
     */
    public Double getRadius()
    {
        return radius;
    }

    /**
     * @param string
     */
    public void setBrand(String string)
    {
        brand = string;
    }

    /**
     * @param integer
     */
    public void setSeeds(Integer integer)
    {
        seeds = integer;
    }

    /**
     * @param double1
     */
    public void setRadius(Double double1)
    {
        radius = double1;
    }

    public String getState()
    {
        return state;
    }

    public void start()
    {
        state = "started";
    }

    public void stop()
    {
        state = "stopped";
    }

    public void dispose()
    {
        state = "disposed";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof WaterMelon) {
            WaterMelon melon = (WaterMelon) obj;
            return (getBrand().equals(melon.getBrand()) && getRadius().equals(melon.getRadius())
                    && getSeeds().equals(melon.getSeeds()) && getState().equals(getState()));
        }

        return super.equals(obj);
    }

}
