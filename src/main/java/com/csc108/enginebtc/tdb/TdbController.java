package com.csc108.enginebtc.tdb;

import cn.com.wind.td.tdb.*;
import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.utils.ConfigUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class TdbController extends AbstractLifeCircleBean {

    private static final Logger logger = LoggerFactory.getLogger(TdbController.class);


    private static final String PROPERTY_CONFIG_FILE = "application.properties";

    private static final String L2_IP_PROPERTY_NAME = "tdb.l2.ip";
    private static final String L2_PORT_PROPERTY_NAME = "tdb.l2.port";
    private static final String L2_USER_PROPERTY_NAME = "tdb.l2.user";
    private static final String L2_PASSWORD_PROPERTY_NAME = "tdb.l2.password";


    // Default setting for data retrieval.
    private static final int DEFAULT_BEGIN_TIME = 0;
    private static final int DEFAULT_END_TIME = 235959000;


    private final OPEN_SETTINGS L2_TdbSettings = new OPEN_SETTINGS();
    private TDBClient client = new TDBClient();

    public static TdbController TdbController = new TdbController();


    private TdbController() {
        this.config();
    }


    @Override
    public void config() {
        String configFile = ConfigUtil.getConfigPath(PROPERTY_CONFIG_FILE);
        try {
            Configuration config = new PropertiesConfiguration(configFile);
            L2_TdbSettings.setIP(config.getString(L2_IP_PROPERTY_NAME));
            L2_TdbSettings.setPort(config.getString(L2_PORT_PROPERTY_NAME));
            L2_TdbSettings.setUser(config.getString(L2_USER_PROPERTY_NAME));
            L2_TdbSettings.setPassword(config.getString(L2_PASSWORD_PROPERTY_NAME));


        } catch (ConfigurationException e) {
            throw new InitializationException("Failed to read config for tdb initialization", e);
        }
    }

    @Override
    public void start() {
        logger.info("Connecting to TDB: " + L2_TdbSettings.getIP() + "-" + L2_TdbSettings.getPort());
        ResLogin res = client.open(L2_TdbSettings);
        if (res == null) {
            client.close();
            throw new InitializationException("Failed to open TDB connect." + L2_TdbSettings.getIP() + ":" + L2_TdbSettings.getPort());
        }
    }

    @Override
    public void stop() {

    }


    public TickAB[] getTick(String stockId, int date) {
        ReqTick reqTick = new ReqTick();
        reqTick.setBeginDate(date);
        reqTick.setEndDate(date);
        reqTick.setBeginTime(DEFAULT_BEGIN_TIME);
        reqTick.setEndTime(DEFAULT_END_TIME);
        reqTick.setCode(stockId);

        TickAB[] ticks = client.getTickAB(reqTick);
        return ticks;
    }

    public Transaction[] getTransaction(String stockId, int date) {
        ReqTransaction reqTransaction = new ReqTransaction();
        reqTransaction.setBeginDate(date);
        reqTransaction.setEndDate(date);
        reqTransaction.setBeginTime(DEFAULT_BEGIN_TIME);
        reqTransaction.setEndTime(DEFAULT_END_TIME);
        reqTransaction.setCode(stockId);
        Transaction[] transactions = client.getTransaction(reqTransaction);

        return transactions;
    }

}

