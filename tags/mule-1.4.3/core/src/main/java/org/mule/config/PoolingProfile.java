/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config;

import org.mule.config.pool.CommonsPoolFactory;
import org.mule.umo.model.UMOPoolFactory;
import org.mule.util.MapUtils;
import org.mule.util.ObjectPool;

import java.util.Map;

import org.apache.commons.collections.map.CaseInsensitiveMap;

/**
 * <code>PoolingProfile</code> is a configuration object used to define the object
 * pooling parameters for the component it is associated with.
 */

public class PoolingProfile
{

    /**
     * Tells the object pool not to initialise any components on startup.
     */
    public static final int INITIALISE_NONE = 0;
    /** @deprecated use INITIALISE_NONE instead */
    public static final int POOL_INITIALISE_NO_COMPONENTS = INITIALISE_NONE;

    /**
     * Tells the object pool only to initialise one component on startup.
     */
    public static final int INITIALISE_ONE = 1;
    /** @deprecated use INITIALISE_ONE instead */
    public static final int POOL_INITIALISE_ONE_COMPONENT = INITIALISE_ONE;

    /**
     * Tells the object pool to initialise all components on startup.
     */
    public static final int INITIALISE_ALL = 2;
    /** @deprecated use INITIALISE_ALL instead */
    public static final int POOL_INITIALISE_ALL_COMPONENTS = INITIALISE_ALL;

    /**
     * Controls the maximum number of Mule UMOs that can be borrowed from a component
     * pool at one time. When non-positive, there is no limit to the number of
     * components that may be active at one time. When maxActive is exceeded, the
     * pool is said to be exhausted. You can specify this value on the descriptor
     * declaration. If none is set this value will be used.
     */
    public static final int DEFAULT_MAX_POOL_ACTIVE = ObjectPool.DEFAULT_MAX_SIZE;

    /**
     * Controls the maximum number of Mule UMOs that can sit idle in the pool at any
     * time. When non-positive, there is no limit to the number of Mule UMOs that may
     * be idle at one time. You can specify this value on the descriptor declaration.
     * If none is set this value will be used. If this value is not set then a system
     * default of '5' will be used.
     */
    public static final int DEFAULT_MAX_POOL_IDLE = ObjectPool.DEFAULT_MAX_SIZE;

    /**
     * When the threadPoolExhaustedAction is set to WHEN_EXHAUSTED_WAIT this can
     * specify the maximum milliseconds the pool should block before throwing a
     * NoSuchElementException
     */
    public static final long DEFAULT_MAX_POOL_WAIT = ObjectPool.DEFAULT_MAX_WAIT;

    /**
     * Specifies the behaviour of the Mule UMO pool when the pool is exhausted:
     * <ul>
     * <li>WHEN_EXHAUSTED_FAIL : will throw a NoSuchElementException</li>
     * <li>WHEN_EXHAUSTED_WAIT : will block (invoke Object.wait(long) until a new or
     * idle object is available.</li>
     * <li>WHEN_EXHAUSTED_GROW : will create a new Mule and return it (essentially
     * making maxActive meaningless).</li>
     * </ul>
     * If a positive maxWait value is supplied, it will block for at most that many
     * milliseconds, after which a NoSuchElementException will be thrown. If maxWait
     * is non-positive, it will block indefinitely.
     */
    public static final int DEFAULT_POOL_EXHAUSTED_ACTION = ObjectPool.WHEN_EXHAUSTED_GROW;

    /**
     * Determines how components in a pool should be initialised. The possible values
     * are:
     * <ul>
     * <li>INITIALISE_NONE : Will not load any components in the pool on startup</li>
     * <li>INITIALISE_ONE : Will load only the first component in the pool on
     * startup</li>
     * <li>INITIALISE_ALL : Will load all components in the pool on startup</li>
     * </ul>
     */
    public static final int DEFAULT_POOL_INITIALISATION_POLICY = INITIALISE_ONE;

    // map pool exhaustion strings to their respective values
    private static final Map POOL_EXHAUSTED_ACTIONS = new CaseInsensitiveMap()
    {
        private static final long serialVersionUID = 1L;

        // static initializer
        {
            // if the values were an actual enum in ObjectPool we could iterate
            // properly.. :/

            Integer value = new Integer(ObjectPool.WHEN_EXHAUSTED_WAIT);
            this.put("WHEN_EXHAUSTED_WAIT", value);
            this.put("WAIT", value);
            // TODO HH: remove for 2.0 (only keep WAIT)
            this.put("BLOCK", value);

            value = new Integer(ObjectPool.WHEN_EXHAUSTED_FAIL);
            this.put("WHEN_EXHAUSTED_FAIL", value);
            this.put("FAIL", value);

            value = new Integer(ObjectPool.WHEN_EXHAUSTED_GROW);
            this.put("WHEN_EXHAUSTED_GROW", value);
            this.put("GROW", value);
        }
    };

    // map pool initialisation policy strings to their respective values
    private static final Map POOL_INITIALISATION_POLICIES = new CaseInsensitiveMap()
    {
        private static final long serialVersionUID = 1L;

        // static initializer
        {
            Integer value = new Integer(INITIALISE_NONE);
            this.put("INITIALISE_NONE", value);

            value = new Integer(INITIALISE_ONE);
            this.put("INITIALISE_ONE", value);
            // TODO HH: remove for 2.0 (only keep INITIALISE_ONE)
            this.put("INITIALISE_FIRST", value);

            value = new Integer(INITIALISE_ALL);
            this.put("INITIALISE_ALL", value);
        }
    };

    private int maxActive = DEFAULT_MAX_POOL_ACTIVE;

    private int maxIdle = DEFAULT_MAX_POOL_IDLE;

    private long maxWait = DEFAULT_MAX_POOL_WAIT;

    private int exhaustedAction = DEFAULT_POOL_EXHAUSTED_ACTION;

    private int initialisationPolicy = DEFAULT_POOL_INITIALISATION_POLICY;

    private UMOPoolFactory poolFactory = new CommonsPoolFactory();

    public PoolingProfile()
    {
        super();
    }

    public PoolingProfile(PoolingProfile pp)
    {
        this.maxActive = pp.getMaxActive();
        this.maxIdle = pp.getMaxIdle();
        this.maxWait = pp.getMaxWait();
        this.exhaustedAction = pp.getExhaustedAction();
        this.initialisationPolicy = pp.getInitialisationPolicy();
        if (pp.getPoolFactory() != null)
        {
            poolFactory = pp.getPoolFactory();
        }
    }

    public PoolingProfile(int maxActive,
                          int maxIdle,
                          long maxWait,
                          int exhaustedAction,
                          int initialisationPolicy)
    {
        this.maxActive = maxActive;
        this.maxIdle = maxIdle;
        this.maxWait = maxWait;
        this.exhaustedAction = exhaustedAction;
        this.initialisationPolicy = initialisationPolicy;
    }

    /**
     * @return max number of Mule UMOs that can be idle in a component
     */
    public int getMaxIdle()
    {
        return maxIdle;
    }

    /**
     * @return max number of Mule UMOs that can be active in a component
     */
    public int getMaxActive()
    {
        return maxActive;
    }

    /**
     * @return time in miilisconds to wait for a Mule UMO to be available in a
     *         component when the pool of Mule UMOs is exhausted and the
     *         PoolExhaustedAction is set to WHEN_EXHAUSTED_BLOCK
     */
    public long getMaxWait()
    {
        return maxWait;
    }

    /**
     * @return the action when the Mule UMO pool is exhaused for a component
     */
    public int getExhaustedAction()
    {
        return exhaustedAction;
    }

    public int getInitialisationPolicy()
    {
        return initialisationPolicy;
    }

    public void setInitialisationPolicy(int policy)
    {
        initialisationPolicy = policy;
    }

    public void setMaxIdle(int maxIdle)
    {
        this.maxIdle = maxIdle;
    }

    public void setMaxActive(int maxActive)
    {
        this.maxActive = maxActive;
    }

    public void setMaxWait(long maxWait)
    {
        this.maxWait = maxWait;
    }

    public void setExhaustedAction(int exhaustedAction)
    {
        this.exhaustedAction = exhaustedAction;
    }

    public void setExhaustedActionString(String poolExhaustedAction)
    {
        this.exhaustedAction = MapUtils.getIntValue(POOL_EXHAUSTED_ACTIONS, poolExhaustedAction,
            ObjectPool.DEFAULT_EXHAUSTED_ACTION);
    }

    public void setInitialisationPolicyString(String policy)
    {
        this.initialisationPolicy = MapUtils.getIntValue(POOL_INITIALISATION_POLICIES, policy,
            DEFAULT_POOL_INITIALISATION_POLICY);
    }

    public UMOPoolFactory getPoolFactory()
    {
        return poolFactory;
    }

    public void setPoolFactory(UMOPoolFactory poolFactory)
    {
        this.poolFactory = poolFactory;
    }

}
