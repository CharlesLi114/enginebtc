package com.csc108.enginebtc.controller;

import com.csc108.enginebtc.admin.NettySender;
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

import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
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


    private int speed;
    private int warmupSecs;
    private int stepInMillis;

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
        int orderMinTimeStamp = OrderCache.OrderCache.getMinTimestamp();
        int minTimeStamp = TimeUtils.addSeconds(orderMinTimeStamp, -warmupSecs);
        SyncUtils.syncWithCalc(minTimeStamp, calcs, speed);
        this.waitForCalc();
        this.syncWithEngine(minTimeStamp);

        ReplayController.Replayer.init(minTimeStamp, speed, stepInMillis);
        ReplayController.Replayer.start();
    }



    /**
     * Wait until calc is ready.
     */
    private void waitForCalc() {

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
    public void waitCompsReady() {
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

    public boolean isCalcReady() {
        return this.isCalcReady;
    }

    public void setCalcReady() {
        this.isCalcReady = true;
    }

    public void setSystemReady() {
        this.isSystemReady = true;
    }


}
