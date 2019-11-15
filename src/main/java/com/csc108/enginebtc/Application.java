package com.csc108.enginebtc;

import com.csc108.enginebtc.amq.ActiveMqController;
import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.cache.TdbDataCache;
import com.csc108.enginebtc.controller.Controller;
import com.csc108.enginebtc.fix.InitiatorApp;
import com.csc108.enginebtc.tdb.TdbController;
import com.csc108.enginebtc.utils.ConfigUtil;
import com.csc108.enginebtc.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.IOException;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class Application {

    static {
        System.setProperty("log.home", "D:/logs/BackTestSuit/Controller/" + TimeUtils.getActionDay() + "/");
    }



    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static Application Application = new Application();

    private static ThreadedSocketInitiator OmInitiator;
    private static InitiatorApp omInitiatorApplication;



    // Calc Tdb read data ready.

    private volatile boolean isCalcStockReceived = false;



    public void waitForCalcStockReceived() {

    }



    public void setCalcStockReceived() {
        isCalcStockReceived = true;
    }


    private static void startInitiator () throws Exception{
        String configFile = ConfigUtil.getConfigPath("initiator.cfg");
//        SessionSettings settings = new SessionSettings("configuration/initiator.cfg");
        SessionSettings settings = new SessionSettings(configFile);
        FileStoreFactory fileStoreFactory = new FileStoreFactory(settings);
        FileLogFactory logFactory = new FileLogFactory(settings);
        DefaultMessageFactory defaultMessageFactory = new DefaultMessageFactory();
        omInitiatorApplication = new InitiatorApp();
        OmInitiator = new ThreadedSocketInitiator(omInitiatorApplication,fileStoreFactory,settings, logFactory,defaultMessageFactory);
        OmInitiator.start();
    }


    public void work() {
        ActiveMqController.Controller.start();

        TdbController.TdbController.start();
        OrderCache.OrderCache.start();

        Controller.Controller.waitCompsStarted();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Controller.Controller.syncStockCodeWithCalc();
        TdbDataCache.TdbCache.readTdb(OrderCache.OrderCache.getStockIds(), OrderCache.OrderCache.getDate());
        Controller.Controller.waitForCalcDataReady();


        Controller.Controller.start();
    }




    public static void main(String[] args) throws Exception {

//        Application.work();
        startInitiator();
        Thread.sleep(10000000);

    }



}
