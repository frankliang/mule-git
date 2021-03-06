/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jdbc.sqlstrategy;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transaction.Transaction;
import org.mule.transaction.TransactionCoordination;
import org.mule.transport.jdbc.JdbcConnector;
import org.mule.transport.jdbc.JdbcUtils;
import org.mule.util.ArrayUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
/**
 * Implements strategy for handling individual insert, update, and delete statements
 *
 */
public  class SimpleUpdateSQLStrategy  implements SQLStrategy
{
    protected static Logger logger = Logger.getLogger(SelectSQLStrategy.class);

    public MuleMessage executeStatement(JdbcConnector connector,
            ImmutableEndpoint endpoint, MuleEvent event,long timeout) throws Exception
    {
        //Unparsed SQL statement (with #[foo] format parameters)
        String statement = connector.getStatement(endpoint);

        //Storage for parameters
        List paramNames = new ArrayList();
        
        //Parsed SQL statement (with ? placeholders instead of #[foo] params)
        String sql = connector.parseStatement(statement, paramNames);
        
        //Optionally escape or further manipulate SQL statement.  Used in subclasses.
        sql = escapeStatement(sql);
        
        //Get parameter values from message
        MuleMessage message = event.getMessage();
        Object[] paramValues = connector.getParams(endpoint, paramNames, new DefaultMuleMessage(
            event.transformMessage(), message), endpoint.getEndpointURI().getAddress());

        Transaction tx = TransactionCoordination.getInstance().getTransaction();
        Connection con = null;
            
        try
        {
            con = connector.getConnection();
            
            
            if (logger.isDebugEnabled())
            {
                logger.debug("SQL UPDATE: " + sql + ", params = " + ArrayUtils.toString(paramValues));
            }
            
            int nbRows = connector.getQueryRunner().update(con, sql, paramValues);
            
            if (nbRows != 1)
            {
                logger.warn("Row count for write should be 1 and not " + nbRows);
            }
            if (tx == null)
            {
                JdbcUtils.commitAndClose(con);
            }
            logger.debug("MuleEvent dispatched succesfuly");
        }
        catch (Exception e)
        {
            logger.debug("Error dispatching event: " + e.getMessage(), e);
            if (tx == null)
            {
                JdbcUtils.rollbackAndClose(con);
            }
            throw e;
        }
        
        return event.getMessage();
    }
    
    protected String escapeStatement(String statement)
    {
        //no escaping needed for normal SQL statement
        return statement;
    }
}
