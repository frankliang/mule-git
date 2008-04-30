/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jdbc.functional;

import org.mule.api.MuleMessage;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.jdbc.JdbcConnector;
import org.mule.transport.jdbc.util.MuleDerbyUtils;

import java.util.List;

import org.apache.commons.dbutils.QueryRunner;

public abstract class AbstractJdbcFunctionalTestCase extends FunctionalTestCase
{
    protected static final String[] TEST_VALUES = {"Test", "The Moon", "Terra"};

    private boolean populateTestData = true;
    
    JdbcConnector jdbcConnector = null;
    
    protected String getConfigResources()
    {
        return "jdbc-connector.xml";
    }

    // @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();

        jdbcConnector = (JdbcConnector) muleContext.getRegistry().lookupConnector("jdbcConnector");

        try
        {
            deleteTable();
        }
        catch (Exception e)
        {
            createTable();
        }

        if (populateTestData)
        {
            populateTable();
        }
    }

    // @Override
    protected void doTearDown() throws Exception
    {
        deleteTable();

        super.doTearDown();
    }

    protected void createTable() throws Exception
    {
        QueryRunner qr = jdbcConnector.getQueryRunner();
        qr.update(jdbcConnector.getConnection(), "CREATE TABLE TEST(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0)  NOT NULL PRIMARY KEY,TYPE INTEGER,DATA VARCHAR(255),ACK TIMESTAMP,RESULT VARCHAR(255))");
        logger.debug("Table created");
    }
    
    protected void deleteTable() throws Exception
    {
        QueryRunner qr = jdbcConnector.getQueryRunner();
        int updated = qr.update(jdbcConnector.getConnection(), "DELETE FROM TEST");
        logger.debug(updated + " rows deleted");
    }
    
    protected void populateTable() throws Exception
    {
        QueryRunner qr = jdbcConnector.getQueryRunner();
        int updated;
        updated = qr.update(jdbcConnector.getConnection(), "INSERT INTO TEST(TYPE, DATA) VALUES (1, '" + TEST_VALUES[0] + "')");
        logger.debug(updated + " rows updated");
        updated = qr.update(jdbcConnector.getConnection(), "INSERT INTO TEST(TYPE, DATA) VALUES (2, '" + TEST_VALUES[1] + "')");
        logger.debug(updated + " rows updated");
        updated = qr.update(jdbcConnector.getConnection(), "INSERT INTO TEST(TYPE, DATA) VALUES (3, '" + TEST_VALUES[2] + "')");
        logger.debug(updated + " rows updated");
    }
    
    // @Override
    protected void suitePreSetUp() throws Exception
    {
        MuleDerbyUtils.defaultDerbyCleanAndInit("derby.properties", "database.name");
        super.suitePreSetUp();
    }

    /*
     * org.apache.commons.dbutils.ResultSetHandler (called by QueryRunner which is
     * called by JdbcMessageReceiver) allows either null or a List of 0 rows to be
     * returned so we check for both.
     */
    protected static void assertResultSetEmpty(MuleMessage message)
    {
        assertNotNull(message);
        Object payload = message.getPayload();
        assertTrue(payload instanceof java.util.List);
        List list = (List)payload;
        assertTrue(list.isEmpty());
    }

    protected static void assertResultSetNotEmpty(MuleMessage message)
    {
        assertNotNull(message);
        Object payload = message.getPayload();
        assertTrue(payload instanceof java.util.List);
        List list = (List)payload;
        assertFalse(list.isEmpty());
    }

    public boolean isPopulateTestData()
    {
        return populateTestData;
    }

    public void setPopulateTestData(boolean populateTestData)
    {
        this.populateTestData = populateTestData;
    }
}


