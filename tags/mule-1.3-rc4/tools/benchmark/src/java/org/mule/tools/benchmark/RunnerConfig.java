/*
 * $Id$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.tools.benchmark;

import org.mule.util.StringUtils;

/**
 * <code>RunnerConfig</code> configuration options for the Benchmark runner
 *
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class RunnerConfig
{
    public static final String ARG_MESSAGES = "-messages";
    public static final String ARG_MESSAGE_SIZE = "-size";
    public static final String ARG_THREADS = "-threads";
    public static final String ARG_CONNECTOR_THREADS = "-connectorThreads";
    public static final String ARG_QUEUE = "-queue";
    public static final String ARG_EXECUTION_TIME = "-execTime";
    public static final String ARG_SYNCHRONOUS = "-sync";
    public static final String ARG_ENDPOINTS = "-endpoints";
    public static final String ARG_MODEL = "-model";
    public static final String ARG_CONNECTOR_CONFIG = "-connectorConfig";

    private int messages = 1000;
    private int messageSize = 1024;
    private int threads = 5;
    private int connectorThreads = 5;
    private int queue = 1000;
    private long executionTime = 0;
    private boolean synchronous = false;
    private String endpoints = null;
    private String[] endpointsArray = new String[]{};

    private String model = "default";
    private String connectorConfig;

    public RunnerConfig()
    {
        super();
    }

    public RunnerConfig(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            System.out.println(args[i]);

        }
        setMessages(getIntOpt(args, ARG_MESSAGES, getMessages()));
        setMessageSize(getIntOpt(args, ARG_MESSAGE_SIZE, getMessageSize()));
        setThreads(getIntOpt(args, ARG_THREADS, getThreads()));
        setConnectorThreads(getIntOpt(args, ARG_CONNECTOR_THREADS, getConnectorThreads()));
        setQueue(getIntOpt(args, ARG_QUEUE, getQueue()));
        setExecutionTime(getLongOpt(args, ARG_EXECUTION_TIME, getExecutionTime()));
        setSynchronous(getBooleanOpt(args, ARG_SYNCHRONOUS));
        setEndpoints(getOpt(args, ARG_ENDPOINTS, getEndpoints()));
        setModel(getOpt(args, ARG_MODEL, getModel()));
        setConnectorConfig(getOpt(args, ARG_CONNECTOR_CONFIG, getConnectorConfig()));
    }

    private int getIntOpt(String[] args, String name, int defaultValue)
    {
        String temp = getOpt(args, name, null);
        if (temp == null)
        {
            return defaultValue;
        } else
        {
            return Integer.parseInt(temp);
        }
    }

    private boolean getBooleanOpt(String[] args, String name )
    {
        String temp = getOpt(args, name, null);
        return temp != null;
    }

    private long getLongOpt(String[] args, String name, long defaultValue)
    {
        String temp = getOpt(args, name, null);
        if (temp == null)
        {
            return defaultValue;
        } else
        {
            return Long.parseLong(temp);
        }
    }

    private String getOpt(String[] args, String name, String defaultValue)
    {
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals(name))
            {
                if (i + 1 >= args.length)
                {
                    return defaultValue;
                } else
                {
                    return args[i + 1];
                }
            }
        }
        return defaultValue;
    }

    public int getConnectorThreads()
    {
        return connectorThreads;
    }

    public void setConnectorThreads(int connectorThreads)
    {
        this.connectorThreads = connectorThreads;
    }

    public String getEndpoints()
    {
        return endpoints;
    }

    public void setEndpoints(String endpoints)
    {
        this.endpoints = endpoints;
        if(endpoints!=null) {
            endpointsArray = StringUtils.split(endpoints,  ",");
        } else {
            throw new IllegalArgumentException("you must specify at least one endpoint");
        }
    }

    public String[] getEndpointsArray()
    {
        return endpointsArray;
    }

    public boolean isSynchronous()
    {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous)
    {
        this.synchronous = synchronous;
    }

    public int getMessages()
    {
        return messages;
    }

    public void setMessages(int messages)
    {
        this.messages = messages;
    }

    public int getMessageSize()
    {
        return messageSize;
    }

    public void setMessageSize(int messageSize)
    {
        this.messageSize = messageSize;
    }

    public int getThreads()
    {
        return threads;
    }

    public void setThreads(int threads)
    {
        this.threads = threads;
    }

    public int getQueue()
    {
        return queue;
    }

    public void setQueue(int queue)
    {
        this.queue = queue;
    }

    public long getExecutionTime()
    {
        return executionTime;
    }

    public void setExecutionTime(long executionTime)
    {
        this.executionTime = executionTime;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getConnectorConfig() {
        return connectorConfig;
    }

    public void setConnectorConfig(String connectorConfig) {
        this.connectorConfig = connectorConfig;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Messages=").append(messages).append("\n");
        buffer.append("Message Size=").append(messageSize).append("\n");
        buffer.append("Threads=").append(threads).append("\n");
        buffer.append("Connector Threads=").append(connectorThreads).append("\n");
        buffer.append("Queue=").append(queue).append("\n");
        buffer.append("Synchronous=").append(synchronous).append("\n");
        buffer.append("Endpoints=").append(endpoints).append("\n");
        buffer.append("Model=").append(model).append("\n");
        buffer.append("Connector Config=").append(connectorConfig).append("\n");
        buffer.append("ExecTime=").append(executionTime);
        return buffer.toString();
    }

    public static void usage()
    {
        System.out.println("Mule Benchmark Test parameters:");
        System.out.println("-messages : Number of messages");
        System.out.println("-size : Message size in bytes");
        System.out.println("-endpoints : a comma separated list of endpoints");
        System.out.println("-threads : Thead pool size");
        System.out.println("-connectorThreads : Connector Thead pool size");
        System.out.println("-queue : event queue size");
        System.out.println("-execTime : Component execution time in ms");
        System.out.println("-synchronous : Run synchronously");
        System.out.println("-model : The model type to use i.e. seda, jms, pipeline");
        System.out.println("-connectorConfig : A Property file containing Connector information such as Jndi settings");
    }
}
