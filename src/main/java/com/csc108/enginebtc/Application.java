package com.csc108.enginebtc;

import com.csc108.enginebtc.amq.ActiveMqController;
import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.cache.TdbDataCache;
import com.csc108.enginebtc.controller.Controller;
import com.csc108.enginebtc.tdb.TdbController;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class Application {






    public static void main(String[] args) throws InterruptedException {

        ActiveMqController.Controller.start();
        TdbController.TdbController.start();
        OrderCache.OrderCache.init();
        TdbDataCache.TdbCache.init(OrderCache.OrderCache.getStockIds(), OrderCache.OrderCache.getDate());

        Controller.Controller.start();




    }


}
