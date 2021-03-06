/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jdbc.xa;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

/**
 * TODO
 */
public class DataSourceWrapper implements DataSource
{

    private XADataSource xads;
    private TransactionManager tm;

    public DataSourceWrapper()
    {
        super();
    }

    public DataSourceWrapper(XADataSource xads, TransactionManager tm)
    {
        this.xads = xads;
        this.tm = tm;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getLoginTimeout()
     */
    public int getLoginTimeout() throws SQLException
    {
        return xads.getLoginTimeout();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(int seconds) throws SQLException
    {
        xads.setLoginTimeout(seconds);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() throws SQLException
    {
        return xads.getLogWriter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        xads.setLogWriter(out);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException
    {
        final ConnectionWrapper connWrapper = new ConnectionWrapper(xads.getXAConnection(), tm);
        connWrapper.setAutoCommit(false);
        return connWrapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    public Connection getConnection(String username, String password) throws SQLException
    {
        final ConnectionWrapper connWrapper = new ConnectionWrapper(xads.getXAConnection(username, password), tm);
        connWrapper.setAutoCommit(false);
        return connWrapper;
    }

    /**
     * @return Returns the transaction manager.
     */
    public TransactionManager getTransactionManager()
    {
        return tm;
    }

    /**
     * @param tm The transaction manager to set.
     */
    public void setTransactionManager(TransactionManager tm)
    {
        this.tm = tm;
    }

    /**
     * @return Returns the underlying XADataSource.
     */
    public XADataSource getXaDataSource()
    {
        return xads;
    }

    /**
     * @param xads The XADataSource to set.
     */
    public void setXaDataSource(XADataSource xads)
    {
        this.xads = xads;
    }
}
