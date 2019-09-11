package com.csc108.enginebtc.amq;

import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.exception.InitializationException;
import com.sun.jndi.ldap.pool.PooledConnectionFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;

/**
 * Created by LI JT on 2019/9/11.
 * Description:
 */
public class ActiveMqController extends AbstractLifeCircleBean {

    private static final Logger logger = LoggerFactory.getLogger(ActiveMqController.class);

    private static final String CONFIG_FILE = "configuration/config.xml";

    private final static int DEFAULT_CONNECTION_IDLE_TIMEOUT = 24 * 60 * 60 * 1000;
    private final static String HQ_TOPIC_NAME = "quotahq";
    private static final long MESSAGE_TIME_TO_LIVE = 1000 * 60 * 5; // 5 minute, discard it.

    public static final ActiveMqController Controller = new ActiveMqController();

    private PooledConnectionFactory pooledFac;
    private ActiveMqConfig config;
    private FileWriter writer;
    private final String WriteSync = "";

    private XMLConfiguration configuration;



    private void initMqConfig() {
        try {
            this.configuration = new XMLConfiguration(CONFIG_FILE);
        } catch (ConfigurationException e) {
            logger.error("Failed to read xml configuration for activemq.", e);
            throw new InitializationException("Failed to read xml configuration for activemq.", e);
        }

        SubnodeConfiguration subConfig = this.configuration.configurationAt("ActiveMQ");
        for (HierarchicalConfiguration node : subConfig.configurationsAt("Connection")) {
            String name              = node.getString("[@name]");
            String topics            = node.getString("topics");
            String server            = node.getString("server");
            String port              = node.getString("port");
            String protocol          = node.getString("protocol");
            String defaultParams     = node.getString("defaultParams");

            int reconnectDelay       = node.getInt("maxReconnectDelay");
            int maxReconnectAttempt  = node.getInt("maxReconnectAttempts");
            int connectionPerFactory = node.getInt("connectionPerFactory");
            int sessionPerConnection = node.getInt("sessionPerConnection");
            boolean failover         = node.getBoolean("failover");
            int timeout              = node.getInt("timeout");
            boolean isLogHq          = node.getBoolean("logHq");
            String logHqStocks       = node.getString("hqLogStocks");
            String hqLogFolder         = node.getString("hqLogFolder");

            this.config = new ActiveMqConfig(name, topics, server, port, protocol, defaultParams, reconnectDelay, maxReconnectAttempt, connectionPerFactory, sessionPerConnection, failover, timeout, isLogHq, logHqStocks, hqLogFolder);
        }
    }




    @Override
    public void config() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
