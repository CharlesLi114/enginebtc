package com.csc108.enginebtc;

import com.csc108.enginebtc.admin.NettyListener;
import com.csc108.enginebtc.admin.NettySender;
import com.csc108.enginebtc.utils.SyncUtils;
import org.quartz.SchedulerException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LI JT on 2019/11/13.
 * Description:
 */
public class TestSendToCalc {

    public static void main(String[] args) {



        List<String> calcs = new ArrayList<>();
        calcs.add("10.101.195.9:9202");

        SyncUtils.syncWithCalc(92900000, calcs, 1);

//        List<String> stocks = new ArrayList<>();
//        stocks.add("600000.SH;000001.SZ");
//        SyncUtils.syncStocksWithCalc(calcs, stocks, 20191112, 130500000);

    }

//    public static void main(String[] args) throws SchedulerException, InterruptedException {
//
//        NettySender sender = new NettySender(true);
//        sender.config("10.101.237.68", 9201);
//        sender.start();
//        while (!sender.isReady()) {
//            Thread.sleep(1000);
//            System.out.println("Wait for connection.");
//        }
//        sender.writeMessage("data amq list");
//
//        Thread.sleep(10000);
//        sender.stop();
//
//        sender = new NettySender(true);
//        sender.config("10.101.237.68", 9201);
//        sender.start();
//        while (!sender.isReady()) {
//            Thread.sleep(1000);
//            System.out.println("Wait for connection.");
//        }
//        sender.writeMessage("data amq list");
//        Thread.sleep(10000);
//        sender.stop();
//
//
//
//
//    }



}
