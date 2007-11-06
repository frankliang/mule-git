/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jdbc;

import org.mule.extras.client.MuleClient;
import org.mule.impl.MuleMessage;
import org.mule.providers.NullPayload;
import org.mule.providers.jdbc.util.MuleDerbyUtils;
import org.mule.tck.FunctionalTestCase;
import org.mule.umo.UMOMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.dbutils.QueryRunner;

public class JdbcSelectOnOutboundFunctionalTestCase extends FunctionalTestCase
{

    public static final String[] TEST_VALUES = {"Test", "The Moon", "Terra"};

    protected String getConfigResources()
    {
        return "jdbc-select-outbound.xml";
    }

    // @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();

        JdbcConnector jdbcConnector = (JdbcConnector) managementContext.getRegistry().lookupConnector("jdbcConnector");
        QueryRunner qr = jdbcConnector.createQueryRunner();
        int updated;

        try
        {
            updated = qr.update(jdbcConnector.getConnection(), "DELETE FROM TEST");
            logger.debug(updated + " rows deleted");
        }
        catch (Exception e)
        {
            qr.update(jdbcConnector.getConnection(), "CREATE TABLE TEST(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0)  NOT NULL PRIMARY KEY,TYPE INTEGER,DATA VARCHAR(255),ACK TIMESTAMP,RESULT VARCHAR(255))");
            logger.debug("Table created");
        }

        updated = qr.update(jdbcConnector.getConnection(), "INSERT INTO TEST(TYPE, DATA) VALUES (1, '" + TEST_VALUES[0] + "')");
        logger.debug(updated + " rows updated");
        updated = qr.update(jdbcConnector.getConnection(), "INSERT INTO TEST(TYPE, DATA) VALUES (2, '" + TEST_VALUES[1] + "')");
        logger.debug(updated + " rows updated");
        updated = qr.update(jdbcConnector.getConnection(), "INSERT INTO TEST(TYPE, DATA) VALUES (3, '" + TEST_VALUES[2] + "')");
        logger.debug(updated + " rows updated");

    }

    protected void doTearDown() throws Exception
    {
        JdbcConnector jdbcConnector = (JdbcConnector) managementContext.getRegistry().lookupConnector("jdbcConnector");
        QueryRunner qr = jdbcConnector.createQueryRunner();
        int updated = qr.update(jdbcConnector.getConnection(), "DELETE FROM TEST");
        logger.debug(updated + " rows deleted");

        super.doTearDown();
    }

    protected void suitePreSetUp() throws Exception
    {
        MuleDerbyUtils.defaultDerbyCleanAndInit("derby.properties", "database.name");
        super.suitePreSetUp();
    }

    public void testSelectOnOutbound() throws Exception
    {
        MuleClient client = new MuleClient();
        UMOMessage reply = client.send("vm://jdbc.test", new MuleMessage(NullPayload.getInstance()));
        assertNotNull(reply.getPayload());
        assertTrue(reply.getPayload() instanceof ArrayList);
        ArrayList resultList = (ArrayList) reply.getPayload();
        assertTrue(resultList.size() == 1);
        assertTrue(resultList.get(0) instanceof HashMap);
        HashMap resultMap = (HashMap) resultList.get(0);
        assertEquals(new Integer(1), resultMap.get("TYPE"));
        assertEquals(TEST_VALUES[0], resultMap.get("DATA"));
    }

    public void testSelectOnOutboundByPropertyExtractor() throws Exception
    {
        MuleClient client = new MuleClient();
        MyMessage payload = new MyMessage(2);
        UMOMessage reply = client.send("vm://terra", new MuleMessage(payload));
        assertNotNull(reply.getPayload());
        assertTrue(reply.getPayload() instanceof ArrayList);
        ArrayList resultList = (ArrayList) reply.getPayload();
        logger.debug("resultList.size() " + resultList.size());
        assertTrue(resultList.size() == 1);
        assertTrue(resultList.get(0) instanceof HashMap);
        HashMap resultMap = (HashMap) resultList.get(0);
        assertEquals(new Integer(2), resultMap.get("TYPE"));
        assertEquals(TEST_VALUES[1], resultMap.get("DATA"));
    }

    public static class MyMessage implements Serializable
    {

        public MyMessage(int type)
        {
            this.type = type;
        }

        private int type;

        public int getType()
        {
            return type;
        }

        public void setType(int type)
        {
            this.type = type;
        }
    }

}


