package com.csc108.enginebtc.controller;

import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.replay.ReplayController;
import com.csc108.enginebtc.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class Controller extends AbstractLifeCircleBean {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    public static Controller Controller = new Controller();


    private volatile boolean isSystemReady = false;
    private volatile boolean isCalcReady = false;

    private static int speed = 1;
    private static int warmupSecs = 60;
    private static int stepInMillis = 10;

    static {
    // Static initialization

    }




    public static int getSpeed() {
        return speed;
    }

    public static int getWarmupSecs() {
        return warmupSecs;
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




    @Override
    public void config() {

    }

    @Override
    public void start() {
        while (!this.isSystemReady || !this.isCalcReady) {
            logger.info("Wait for system and calc to be ready.");
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                throw new InitializationException("Failed to wait for system ready signal.", e);
            }
        }
        int orderMinTimeStamp = OrderController.OrderController.getMinTimeStamp();
        int minTimeStamp = TimeUtils.addSeconds(orderMinTimeStamp, -warmupSecs);
        ReplayController.Replayer.init(minTimeStamp, speed, stepInMillis);
        ReplayController.Replayer.start();
    }

    @Override
    public void stop() {
        ReplayController.Replayer.stop();
    }
}
