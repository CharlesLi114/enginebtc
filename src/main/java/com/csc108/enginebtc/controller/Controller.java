package com.csc108.enginebtc.controller;

import com.csc108.enginebtc.admin.NettySender;
import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.replay.ReplayController;
import com.csc108.enginebtc.utils.ConfigUtil;
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
 * Description:
 */
public class Controller extends AbstractLifeCircleBean {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private static final String PROPERTY_CONFIG_FILE = "application.properties";
    private static final String Speed_Property_Name = "sys.speed";
    private static final String StepInMillis_Property_Name = "sys.step.millis";
    private static final String WarmupSecs_Property_Name = "sys.warmup.secs";

    private static final String EngineConfig_Property_Name = "engine.admin.port";


    public static Controller Controller = new Controller();


    private volatile boolean isSystemReady = false;
    private volatile boolean isCalcReady = false;

    private int speed;
    private int warmupSecs;
    private int stepInMillis;

    private List<String> engines;




    private Controller() {
        engines = new ArrayList<>();
    }




    @Override
    public void config() {
        String configFile = ConfigUtil.getConfigPath(PROPERTY_CONFIG_FILE);
        try {
            Configuration config = new PropertiesConfiguration(configFile);
            this.speed = config.getInt(Speed_Property_Name);
            this.warmupSecs = config.getInt(WarmupSecs_Property_Name);
            this.stepInMillis = config.getInt(StepInMillis_Property_Name);

            logger.info("Using config ");
            logger.info("Speed : " + speed);
            logger.info("WarmupSecs : " + warmupSecs);
            logger.info("StepInMillis : " + stepInMillis);


            List<Object> engines = config.getList(EngineConfig_Property_Name);
            for (Object o : engines) {
                this.engines.add((String) o);
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
        this.sendToCalc(minTimeStamp);
        this.waitForCalc();
        this.syncWithEngine();

        ReplayController.Replayer.init(minTimeStamp, speed, stepInMillis);
        ReplayController.Replayer.start();
    }

    /**
     * Send ready signal to calc, along with minTimestamp.
     */
    private void sendToCalc(int minTimestamp) {

    }

    /**
     * Wait until calc is ready.
     */
    private void waitForCalc() {
        while (!this.isCalcReady) {
            System.out.println("Wait for calc to be ready.");
            logger.info("Wait for calc to be ready.");
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                throw new InitializationException("Failed to wait for system ready signal.", e);
            }
        }
    }

    /**
     * Sync (time) with engines.
     */
    private void syncWithEngine() {
        for (String engine : this.engines) {
            String[] splits = engine.split(":");
            NettySender sender = new NettySender();
            sender.config(splits[0], Integer.parseInt(splits[1]));
            sender.start();
            while (!sender.isReady()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Wait for connection.");
            }
            sender.writeMessage("data amq list");
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
