package com.csc108.enginebtc.amq;


import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.tdb.models.MarketData;
import com.csc108.enginebtc.tdb.models.TransactionData;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.FileWriter;
import java.util.Map;

/**
 * Created by LI JT on 2019/9/11.
 * Description:
 */
public class ActiveMqController extends AbstractLifeCircleBean {

    private static final Logger logger = LoggerFactory.getLogger(ActiveMqController.class);

    private static final String CONFIG_FILE = "configuration/activemq.xml";

    private final static int DEFAULT_CONNECTION_IDLE_TIMEOUT = 24 * 60 * 60 * 1000;
    private final static String HQ_TOPIC_NAME = "quotahq";
    private final static String TRANS_TOPIC_NAME = "transaction";
    private static final long MESSAGE_TIME_TO_LIVE = 1000 * 60 * 5; // 5 minute, discard it.

    public static final ActiveMqController Controller = new ActiveMqController();

    private PooledConnectionFactory pooledFac;
    private ActiveMqConfig config;
    private FileWriter writer;
    private final String WriteSync = "";

    private XMLConfiguration configuration;


    private ActiveMqController() {
        this.config();
    }


    @Override
    public void config() {
        try {
            this.configuration = new XMLConfiguration(CONFIG_FILE);
        } catch (org.apache.commons.configuration.ConfigurationException e) {
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
    public void start() {
        init();
    }

    @Override
    public void stop() {

    }


    /**
     * Get connection to the hq mq
     * @return connection to mq
     * @throws JMSException
     */
    public Connection getConnection() throws JMSException {
        Connection conn = this.pooledFac.createConnection();
        if(conn.getExceptionListener() == null) {
            conn.setExceptionListener(new ExceptionListener() {
                public void onException(JMSException jmsException) {
                    throw new RuntimeException("Problem found for connection to Active MQ.", jmsException);
                }
            });
        }
        // Note: below start is handled by pooledConnection, no worry about multiple start
        conn.start();
        return conn;
    }

    public void init() {
        String connectionName = config.getName();
        String connectionString = config.getConnStr();
        System.out.println(connectionString);


        ActiveMQConnectionFactory fac = new ActiveMQConnectionFactory(connectionString);

        fac.setCopyMessageOnSend(false); // don't copy as we always create new messages on each publish
        fac.setUseAsyncSend(true);
        fac.setOptimizeAcknowledge(true);
        // blow check with Cai Shijie to confirm the size
        // fac.setAlwaysSyncSend(false);
        // fac.getPrefetchPolicy().setTopicPrefetch(2);

        // TransportListener is not used in this project.
//        AbstractConnectionStateMonitor statusMonitor;
//        if (connectionName.equalsIgnoreCase(ALERT_CONN_NAME)) {
//            statusMonitor = new AlertConnectionStateMonitor(connectionString);
//        } else {
//            statusMonitor = new MessageConnectionStateMonitor(connectionString);
//            if (connectionName.equalsIgnoreCase(REPLY_CONN_NAME)) {
//                ((MessageConnectionStateMonitor)statusMonitor).setAmqAlert(AlertEnum.AMQ_SEND_FAILURE);
//            } else {
//                ((MessageConnectionStateMonitor)statusMonitor).setAmqAlert(AlertEnum.AMQ_RECEIVE_FAILURE);
//            }
//        }
//        fac.setTransportListener(statusMonitor);
//        connectionStatusMap.put(connectionName, statusMonitor);


        // fac.setProducerWindowSize(1024000000);
        // fac.setAlwaysSessionAsync(false); // bypass the session internal message queue to dispatch to consumers directly
        pooledFac = new org.apache.activemq.pool.PooledConnectionFactory(fac);
        pooledFac.setMaxConnections(config.getConnectionPerFactory());
        pooledFac.setMaximumActiveSessionPerConnection(config.getSessionPerConnection());
        pooledFac.setCreateConnectionOnStartup(true); // warm up
        pooledFac.setIdleTimeout(DEFAULT_CONNECTION_IDLE_TIMEOUT); // all day alive
        pooledFac.setBlockIfSessionPoolIsFull(false); // throw exception explicitly when pool resource are exausted
        pooledFac.initConnectionsPool();
        pooledFac.start();
//        amqConfigs.put(connectionName, config);
    }

    /**
     * Get session by topic.
     * @return session.
     * @throws JMSException
     */
    public Session getSession() throws JMSException {
        Connection connection = getConnection();
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Send market data to activemq, in engine format, use filter to identify stock id.
     * TODO to decommission.
     * @param marketData data to send
     */
    public void sendTicks1(MarketData marketData) {
        try {
            if (!marketData.isValid()) {
                return;
            }


            Session session = this.getSession();
            Destination destination = session.createTopic(HQ_TOPIC_NAME);
            MessageProducer producer = session.createProducer(destination);

            String msg = marketData.toXmlMsg();
            Message textMsg = session.createTextMessage(msg);

            // hq000000sz as selector
            textMsg.setStringProperty("myFilter", marketData.getSelector());

            producer.setTimeToLive(MESSAGE_TIME_TO_LIVE);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            producer.send(textMsg);

            session.close();

        } catch (JMSException e) {
            logger.error("Send MarketData failed: ", e);
        }
    }

    /**
     * Send market data using data map.
     * @param marketData
     */
    public void sendTicks(MarketData marketData) {
        try {
            if (!marketData.isValid()) {
                return;
            }

            ActiveMQMapMessage msg = marketData.toMQMapMessage();

            Session session = this.getSession();
            Destination destination = session.createTopic(this.getTopic(marketData.getStockId(), true));
            MessageProducer producer = session.createProducer(destination);
            producer.setTimeToLive(MESSAGE_TIME_TO_LIVE);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            producer.send(msg);

            session.close();
        } catch (JMSException e) {
            logger.error("Send MarketData failed: ", e);
        }
    }

    public void sendTrans(TransactionData transaction) {
        try {
            if (!transaction.isValid()) {
                return;
            }

            ActiveMQMapMessage msg = transaction.toMQMapMessage();
            Session session = this.getSession();
            Destination destination = session.createTopic(this.getTopic(transaction.getStockId(), false));
            MessageProducer producer = session.createProducer(destination);

            producer.setTimeToLive(MESSAGE_TIME_TO_LIVE);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            producer.send(msg);

            session.close();
        } catch (JMSException e) {
            logger.error("Send Transaction failed: ", e);
        }
    }

    private String getTopic(String stockId, boolean isMarketdata) {
        return isMarketdata? "Tick_" + stockId: "Trade_" + stockId;
    }

}
