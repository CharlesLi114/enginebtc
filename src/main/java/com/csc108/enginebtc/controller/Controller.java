package com.csc108.enginebtc.controller;

import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.exception.InvalidParamException;
import com.csc108.enginebtc.replay.ReplayController;
import com.csc108.enginebtc.utils.ConfigUtil;
import com.csc108.enginebtc.utils.SyncUtils;
import com.csc108.enginebtc.utils.TimeUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LI JT on 2019/9/2.
 * Description: This whole class can be merged to Application.
 */
public class Controller extends AbstractLifeCircleBean {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private static final String PROPERTY_CONFIG_FILE = "application.properties";
    private static final String Speed_Property_Name = "sys.speed";
    private static final String StepInMillis_Property_Name = "sys.step.millis";
    private static final String WarmupSecs_Property_Name = "sys.warmup.secs";

    private static final String EngineConfig_Property_Name = "engine.admin.port";
    private static final String CalcConfig_Property_Name = "calc.admin.port";


    public static Controller Controller = new Controller();


    private volatile boolean isSystemReady = false;

    private volatile boolean isCalcDataReady = false;
    private volatile boolean isCalcTimeSet = false;
    private volatile boolean isEngineTimeSet = false;

    private int speed;
    private int warmupSecs;
    private int stepInMillis;
    private int minTimeStamp;

    private List<String> engines;
    private List<String> calcs;


    private Controller() {
        engines = new ArrayList<>();
        calcs = new ArrayList<>();
        this.config();
    }

    @Override
    public void config() {
        String configFile = ConfigUtil.getConfigPath(PROPERTY_CONFIG_FILE);
        try {
            Configuration config = new PropertiesConfiguration(configFile);
            this.speed = config.getInt(Speed_Property_Name);
            this.warmupSecs = config.getInt(WarmupSecs_Property_Name);
            this.stepInMillis = config.getInt(StepInMillis_Property_Name);

            if (this.speed != 1) {
                throw new InvalidParamException("Current not support speed other than 1.");
            }

            logger.info("Using config ");
            logger.info("Speed : " + speed);
            logger.info("WarmupSecs : " + warmupSecs);
            logger.info("StepInMillis : " + stepInMillis);


            List<Object> engines = config.getList(EngineConfig_Property_Name);
            for (Object o : engines) {
                this.engines.add((String) o);
            }


            List<Object> calcs = config.getList(CalcConfig_Property_Name);
            for (Object o : calcs) {
                this.calcs.add((String) o);
            }

        } catch (ConfigurationException e) {
            logger.error("Failed to read config for system config.", e);
            throw new InitializationException("Failed to read config for system config", e);
        }
    }

    @Override
    public void start() {
        this.syncTime();
        OrderCache.OrderCache.publishOrders();
        ReplayController.Replayer.init(this.minTimeStamp, speed, stepInMillis);
        ReplayController.Replayer.start();
    }


    private void syncTime() {
        int orderMinTimeStamp = OrderCache.OrderCache.getMinTimestamp();
        this.minTimeStamp = TimeUtils.addSeconds(orderMinTimeStamp, -warmupSecs);

        SyncUtils.syncWithEngine(minTimeStamp, engines);
        SyncUtils.syncWithCalc(minTimeStamp, calcs, speed);
        this.waitForTimeSynced();
    }

    /**
     *
     */
    public void syncStockCodeWithCalc() {
        Thread thread = new Thread() {
            public void run() {
                logger.info("Sync stock code with calc.");
                OrderCache cache = OrderCache.OrderCache;
                SyncUtils.syncStocksWithCalc(getCalcs(), cache.getStockIds(), cache.getDate(), cache.getMinTimestamp());
            }
        };
        thread.start();
    }




    /**
     * Wait until calc finished reading tdf data.
     */
    public void waitForCalcDataReady() {
        while (!this.isCalcDataReady) {
            System.out.println("Wait for calc to be ready.");
            logger.info("Wait for calc to be ready.");
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                throw new InitializationException("Failed to wait for system ready signal.", e);
            }
        }
    }

    public void setCalcDataReady() {
        isCalcDataReady = true;
    }

    public void setCalcTimeReady() {
        isCalcTimeSet = true;
    }

    public void setEngineTimeReady() {
        isEngineTimeSet = true;
    }


    /**
     * Wait until Calc and Engine are ready, with time drift applied.
     * Calc should send message back saying it has processed the data.
     */
    private void waitForTimeSynced() {
        while (!isEngineTimeSet || !isCalcTimeSet) {
            System.out.println("Wait for time offset to be ready.");
            logger.info("Wait for time offset to be ready.");
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                throw new InitializationException("Failed to wait for system ready signal.", e);
            }
        }
    }

    public List<String> getCalcs() {
        return this.calcs;
    }

    public List<String> getEngines() {
        return this.engines;
    }


    /**
     * Wait until calc and engine are ready, by connecting to their communication port.
     *
     */
    public void waitCompsStarted() {
        for (String config : this.engines) {
            logger.info("Wait for " + config);
            SyncUtils.waitFor(config);
            logger.info(config + " connected.");
        }

        for (String config : this.calcs) {
            logger.info("Wait for " + config);
            SyncUtils.waitFor(config);
            logger.info(config + " connected.");
        }
    }


    @Override
    public void stop() {
        ReplayController.Replayer.stop();
    }

    public boolean isSystemReady() {
        return this.isSystemReady;
    }


    public void setSystemReady() {
        this.isSystemReady = true;
    }


}
