/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.oracle.jms;

import org.mule.umo.lifecycle.InitialisationException;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.jms.JMSException;
import javax.sql.DataSource;

import oracle.jdbc.driver.OracleDriver;
import oracle.jdbc.pool.OracleDataSource;

/**
 * Extends the standard Mule JMS Provider with functionality specific to Oracle's JMS
 * implementation based on Advanced Queueing (Oracle AQ).
 * 
 * @see OracleJmsSupport
 * @see org.mule.providers.jms.JmsConnector
 * @see <a href="http://otn.oracle.com/pls/db102/">Streams Advanced Queuing</a>
 */
public class OracleJmsConnector extends AbstractOracleJmsConnector
{
    /**
     * The JDBC URL for the Oracle database. For example,
     * {@code jdbc:oracle:oci:@myhost}
     */
    private String url;

    /**
     * DataSource used to obtain JDBC connections to the Oracle database.
     */
    private DataSource dataSource;

    public OracleJmsConnector()
    {
        super();
    }

    protected void doInitialise() throws InitialisationException
    {
        try
        {
            // Register the Oracle JDBC driver.
            Driver oracleDriver = new OracleDriver();
            // Deregister first just in case the driver has already been registered.
            DriverManager.deregisterDriver(oracleDriver);
            DriverManager.registerDriver(oracleDriver);

            if (dataSource == null)
            {
                // Since many connections are opened and closed, we use a connection pool to
                // obtain the JDBC connection.
                dataSource = new OracleDataSource();
                ((OracleDataSource) dataSource).setDataSourceName("Mule Oracle AQ Provider");
                ((OracleDataSource) dataSource).setUser(username);
                ((OracleDataSource) dataSource).setPassword(password);
                ((OracleDataSource) dataSource).setURL(url);
            }
        }
        catch (SQLException e)
        {
            throw new InitialisationException(e, this);
        }
        super.doInitialise();
    }

    public java.sql.Connection getJdbcConnection() throws JMSException
    {
        try
        {
            logger.debug("Getting queue/topic connection");
            return dataSource.getConnection();
        }
        catch (SQLException e)
        {
            throw new JMSException("Unable to open JDBC connection: " + e.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ////////////////////////////////////////////////////////////////////////////////////
    
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
}
