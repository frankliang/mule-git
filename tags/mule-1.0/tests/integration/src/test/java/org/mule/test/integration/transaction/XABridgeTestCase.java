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
 */
package org.mule.test.integration.transaction;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.config.builders.MuleXmlConfigurationBuilder;
import org.mule.providers.jdbc.JdbcUtils;
import org.mule.tck.AbstractMuleTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * $Revision$
 */
public class XABridgeTestCase extends AbstractMuleTestCase {

    private static ClassPathXmlApplicationContext context;
    
    private static final Log logger = LogFactory.getLog(XABridgeTestCase.class);

    protected void emptyTable() throws Exception {
    	try {
    		execSqlUpdate("DELETE FROM TEST");
    	} catch (Exception e) {
    		execSqlUpdate("CREATE TABLE TEST(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0)  NOT NULL PRIMARY KEY,TYPE INTEGER,DATA VARCHAR(255),ACK TIMESTAMP,RESULT VARCHAR(255))");
    	}
    }
    
    protected Connection getConnection() throws Exception {
    	Class.forName("org.hsqldb.jdbcDriver");
    	return DriverManager.getConnection("jdbc:hsqldb:mem:.");
    }
    
    protected void setUp() throws Exception
    {
        emptyTable();
        new MuleXmlConfigurationBuilder().configure("org/mule/test/integration/transaction/xabridge-mule.xml");
    }
    
    protected void tearDown() throws Exception {
        if(MuleManager.isInstanciated()) {
        	MuleManager.getInstance().dispose();
        }
    }
    
    protected List execSqlQuery(String sql) throws Exception {
    	Connection con = null;
    	try {
    		con = getConnection();
    		return (List) new QueryRunner().query(con, sql, new ArrayListHandler());
    	} finally {
    		JdbcUtils.close(con);
    	}
    }

    protected int execSqlUpdate(String sql) throws Exception {
    	Connection con = null;
    	try {
    		con = getConnection();
    		return new QueryRunner().update(con, sql);
    	} finally {
    		JdbcUtils.close(con);
    	}
    }

    protected void doTestXaBridge(boolean rollback) throws Exception {
    	XABridgeComponent.mayRollback = rollback;
    	
    	
    	List results = execSqlQuery("SELECT * FROM TEST");
    	assertEquals(0, results.size());
    	
    	for (int i = 0; i < 10; i++) {
    		execSqlUpdate("INSERT INTO TEST(TYPE, DATA) VALUES (1, 'Test " + i + "')");
    	}
		results = execSqlQuery("SELECT * FROM TEST WHERE TYPE = 1");
		assertEquals(10, results.size());
    	
    	long t0 = System.currentTimeMillis();
    	while (true) {
    		results = execSqlQuery("SELECT * FROM TEST WHERE TYPE = 2");
			logger.info("Results found: " + results.size());
    		if (results.size() >= 10) {
    			break;
    		}
    		assertTrue (System.currentTimeMillis() - t0 < 50000);
    		Thread.sleep(500);
    	}
    }

    public void testXaBridgeWithoutRollbacks() throws Exception {
    	doTestXaBridge(false);
    }

    public void testXaBridgeWithRollbacks() throws Exception {
    	doTestXaBridge(true);
    }
}
