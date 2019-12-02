package com.csc108.enginebtc.amq;

import com.csc108.enginebtc.utils.TimeUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Charles on 10/31/2017.
 */
public class ActiveMqConfig {

    private String name;
    private String server;
    private String port;
    private String protocol;
    private String defaultParams;

    private int reconnectDelay;
    private int maxReconnectAttempt;
    private int connectionPerFactory;
    private int sessionPerConnection;
    private boolean failover;
    private int timeout;

    private String connStr;

    private List<String> topics;

    private boolean isLogHq;
    private Set<String> hqStocks;
    private String hqLogFolder;
    private String outTopicPrefix;

    public ActiveMqConfig(String name, String topic, String server, String port, String protocol, String defaultParams,
                          int reconnectDelay, int maxReconnectAttempt, int connectionPerFactory,
                          int sessionPerConnection, boolean failover, int timeout, boolean isLogHq, String hqStocks, String hqLogFolder, String outTopicPrefix) {
        this.name = name;
        this.server = server;
        this.port = port;
        this.protocol = protocol;
        this.defaultParams = defaultParams;
        this.reconnectDelay = reconnectDelay;
        this.maxReconnectAttempt = maxReconnectAttempt;
        this.connectionPerFactory = connectionPerFactory;
        this.sessionPerConnection = sessionPerConnection;
        this.failover = failover;
        this.timeout = timeout;

        connStr = protocol + "://" + server + ":" + port + "?" + defaultParams;
        if(failover) {
            connStr = "failover:(" + protocol + "://" + server + ":" + port + ")?maxReconnectAttempts=" + this.maxReconnectAttempt + "&timeout=" + timeout + "&maxReconnectDelay=" + this.reconnectDelay + "&" + defaultParams;
        }

        String[] splits = topic.split(";");
        this.topics = Arrays.asList(splits);

        this.isLogHq = isLogHq;
        this.hqStocks = new HashSet<>();
        splits = hqStocks.split(";");
        for (String split : splits) {
            this.hqStocks.add(split.toUpperCase());
        }
        this.hqLogFolder = hqLogFolder.replace("${current.date}", String.valueOf(TimeUtils.getActionDay()));
        this.outTopicPrefix = outTopicPrefix;
    }

    public String getName() {
        return name;
    }

    public String getServer() {
        return server;
    }

    public String getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getDefaultParams() {
        return defaultParams;
    }

    public int getReconnectDelay() {
        return reconnectDelay;
    }

    public int getMaxReconnectAttempt() {
        return maxReconnectAttempt;
    }

    public int getConnectionPerFactory() {
        return connectionPerFactory;
    }

    public int getSessionPerConnection() {
        return sessionPerConnection;
    }

    public boolean isFailover() {
        return failover;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getConnStr() {
        return connStr;
    }

    public List<String> getTopics() {
        return topics;
    }

    public boolean logHq(String stockId) {
        return this.isLogHq && this.hqStocks.contains(stockId.toUpperCase().intern());
    }

    public boolean isLogHq() {
        return this.isLogHq;
    }

    public String getHqLogFolder() {
        return this.hqLogFolder;
    }

    public String getOutTopicPrefix() {
        return this.outTopicPrefix;
    }
}
