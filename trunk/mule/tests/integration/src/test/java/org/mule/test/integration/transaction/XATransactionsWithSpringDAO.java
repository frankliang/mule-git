/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.mule.extras.client.MuleClient;
import org.mule.providers.jdbc.JdbcUtils;
import org.mule.tck.FunctionalTestCase;
import org.mule.test.integration.transaction.extras.Book;
import org.mule.umo.UMOMessage;

public class XATransactionsWithSpringDAO extends FunctionalTestCase{

    protected String getConfigResources() {
        return "org/mule/test/integration/transaction/xatransactions-with-spring-dao-config.xml";
    }
           
    protected void doPostFunctionalSetUp() throws Exception
    {
        emptyTable();
    }

    protected void emptyTable() throws Exception
    {
        try
        {
            execSqlUpdate("DELETE FROM BOOK");
        }
        catch (Exception e)
        {
            execSqlUpdate("CREATE TABLE BOOK(ID INTEGER NOT NULL PRIMARY KEY,TITLE VARCHAR(255),AUTHOR VARCHAR(255))");
        }
    }

    protected Connection getConnection() throws Exception
    {
        Class.forName("org.hsqldb.jdbcDriver");
        return DriverManager.getConnection("jdbc:hsqldb:mem:.");
    }

    public List execSqlQuery(String sql) throws Exception
    {
        Connection con = null;
        try
        {
            con = getConnection();
            return (List)new QueryRunner().query(con, sql, new ArrayListHandler());
        }
        finally
        {
            JdbcUtils.close(con);
        }
    }

    protected int execSqlUpdate(String sql) throws Exception
    {
        Connection con = null;
        try
        {
            con = getConnection();
            return new QueryRunner().update(con, sql);
        }
        finally
        {
            JdbcUtils.close(con);
        }
    }

    public void testXATransactionUsingSpringDaoNoRollback() throws Exception
    {
        MuleClient client = new MuleClient();
        Book book = new Book(1,"testBook", "testAuthor");
        client.sendNoReceive("jms://my.queue",book,null);
        UMOMessage result = client.receive("vm://output", 50000);
        assertNotNull(result);
        assertNotNull(result.getPayload());
        assertTrue(((Boolean)result.getPayload()).booleanValue());
        int res = execSqlUpdate("UPDATE BOOK SET TITLE = 'My Test' WHERE TITLE='testBook'");
        if (res < 0)
        {
            fail();
        }
    }
        
    public void testXATransactionUsingSpringDaoWithRollback() throws Exception
    {
        MuleClient client = new MuleClient();
        
        Book book = new Book(1,"testBook", "testAuthor");
        client.sendNoReceive("jms://my.queue",book,null);
        UMOMessage result = client.receive("vm://output", 50000);
        assertNotNull(result);
        assertNotNull(result.getPayload());
        assertTrue(((Boolean)result.getPayload()).booleanValue());
        int res = execSqlUpdate("UPDATE BOOK SET TITLE = 'My Test' WHERE TITLE='testBook'");
        if (res < 0)
        {
            fail();
        }
        
        client.sendNoReceive("jms://my.queue",book,null);
        result = client.receive("vm://output", 5000);
        // need to test that the Spring transaction has really been rolled back... 
        // from log file, it is
        assertNull(result);
    }
}