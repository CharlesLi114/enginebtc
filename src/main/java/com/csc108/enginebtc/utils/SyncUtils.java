package com.csc108.enginebtc.utils;

import com.csc108.enginebtc.admin.NettySender;

import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.List;

/**
 * Created by LI JT on 2019/11/1.
 * Description:
 */
public class SyncUtils {

    /**
     * Sync (time) with engines.
     */
    public static void syncWithEngine(int timestamp, List<String> engines) {
        for (String engine : engines) {
            NettySender sender = getSender(engine);
            int offset = -getEngineOffsetInSec(timestamp);
            String msg = MessageFormat.format("algoMgr config clock -o {0}", offset);
            sender.writeMessage(msg);
            sender.stop();
        }
    }

    /**
     * Sync (time) with calcs.
     * TODO Calc should add an offset like engine did.
     */
    public static void syncWithCalc(int minTimeStamp, List<String> calcs, int speed) {
        for (String calc : calcs) {
            NettySender sender = getSender(calc);
            String msg = MessageFormat.format("BTC_SYNC_{0}_{1}", minTimeStamp, speed);
            sender.writeMessage(msg);
            sender.stop();
        }
    }

    public static void syncStocksWithCalc(List<String> calcs, List<String> stocks) {
        String stock = String.join(";", stocks);
        for (String calc : calcs) {
            NettySender sender = getSender(calc);
            String msg = "BTC_STK_" + stock;
            sender.writeMessage(msg);
            sender.stop();
        }
    }


    public static boolean waitFor(String config) {
        NettySender sender = getSender(config, 100);
        sender.stop();
        return true;
    }


    private static NettySender getSender(String config) {
        return getSender(config, 50);
    }

    private static NettySender getSender(String config, int waitRound) {
        String[] splits = config.split(":");
        NettySender sender = new NettySender();
        sender.config(splits[0], Integer.parseInt(splits[1]));
        sender.start();

        int waitCount = 0;
        while (!sender.isReady()) {
            try {
                Thread.sleep(200);
                waitCount += 1;
                if (waitCount > waitRound) {
                    throw new RuntimeException("Failed to connect to " + config + " after " + waitRound + " attempts.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Wait for calc connection");
        }
        return sender;
    }

    /**
     * Return positive offset in seconds, from @param minTimeStamp to LocalTime.now
     */
    private static int getEngineOffsetInSec(int minTimeStamp) {
        LocalTime minTime = TimeUtils.tsToLt(minTimeStamp);
        LocalTime now = LocalTime.now();
        return now.toSecondOfDay() - minTime.toSecondOfDay();
    }









}