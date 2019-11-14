package com.csc108.enginebtc;

import com.csc108.enginebtc.amq.ActiveMqController;
import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.cache.TdbDataCache;
import com.csc108.enginebtc.controller.Controller;
import com.csc108.enginebtc.tdb.TdbController;
import com.csc108.enginebtc.utils.Constants;
import com.csc108.enginebtc.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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



    // Calc Tdb read data ready.

    private volatile boolean isCalcStockReceived = false;



    public void waitForCalcStockReceived() {

    }



    public void setCalcStockReceived() {
        isCalcStockReceived = true;
    }





    public void work() {
//        ActiveMqController.Controller.start();

        TdbController.TdbController.start();
        OrderCache.OrderCache.start();

        Controller.Controller.waitCompsStarted();

        Controller.Controller.syncStockCodeWithCalc();
        TdbDataCache.TdbCache.readTdb(OrderCache.OrderCache.getStockIds(), OrderCache.OrderCache.getDate());
        Controller.Controller.waitForCalcDataReady();


        Controller.Controller.start();
    }




    public static void main(String[] args) throws InterruptedException {

        Application.work();



    }



}
