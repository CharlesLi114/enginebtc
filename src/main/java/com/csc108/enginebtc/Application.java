package com.csc108.enginebtc;

import com.csc108.enginebtc.amq.ActiveMqController;
import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.cache.TdbDataCache;
import com.csc108.enginebtc.controller.Controller;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.tdb.TdbController;
import com.csc108.enginebtc.utils.SyncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static Application Application = new Application();



    // Calc Tdb read data ready.
    private volatile boolean isCalcDataReady = false;

    /**
     * Wait until calc finished
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

    public void work() {
        ActiveMqController.Controller.start();
        TdbController.TdbController.start();
        OrderCache.OrderCache.init();

        Controller.Controller.waitCompsReady();

        SyncUtils.syncStocksWithCalc(Controller.Controller.getCalcs(), OrderCache.OrderCache.getStockIds());
        TdbDataCache.TdbCache.readTdb(OrderCache.OrderCache.getStockIds(), OrderCache.OrderCache.getDate());

        waitForCalcDataReady();


        Controller.Controller.start();
    }




    public static void main(String[] args) throws InterruptedException {

        Application.work();



    }



}
