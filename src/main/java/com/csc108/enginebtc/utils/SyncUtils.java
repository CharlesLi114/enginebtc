package com.csc108.enginebtc.utils;

import com.csc108.enginebtc.admin.NettySender;
import com.csc108.enginebtc.amq.ActiveMqController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.List;

/**
 * Created by LI JT on 2019/11/1.
 * Description:
 */
public class SyncUtils {

    private static final Logger logger = LoggerFactory.getLogger(SyncUtils.class);

    /**
     * Sync (time) with engines.
     */
    public static void syncWithEngine(int timestamp, List<String> engines) {
        for (String engine : engines) {
            NettySender sender = getSender(engine);
            int offset = -getEngineOffsetInSec(timestamp);
            String msg = MessageFormat.format("algoMgr config clock -o {0}", String.valueOf(offset));
            sender.writeMessage(msg);
//            sender.stop();
        }
    }

    /**
     * Sync (time) with calcs.
     * TODO Calc should add an offset like engine did.
     */
    public static void syncWithCalc(int minTimeStamp, List<String> calcs, int speed) {
        for (String calc : calcs) {
            NettySender sender = getSender(calc);
            int offset = -getEngineOffsetInSec(minTimeStamp);
            String msg = MessageFormat.format("service control clock -o {0}", String.valueOf(offset));
            sender.writeMessage(msg);
//            sender.stop();
        }
    }



    /**
     * Send sync command to calc. TODO start a thread, non-blocking
     * @param calcs
     * @param stocks
     * @param tradeDay
     * @param upto
     */
    public static void syncStocksWithCalc(List<String> calcs, List<String> stocks, int tradeDay, int upto) {
        String stock = String.join(";", stocks);
        for (String calc : calcs) {
            NettySender sender = getSender(calc);
            String msg = MessageFormat.format("service control recovery_stock_data -s {0} -d {1} -t {2}", stock, String.valueOf(tradeDay), String.valueOf(upto));
            sender.writeMessage(msg);
//            sender.stop();
        }
    }



    public static boolean waitFor(String config) {
        NettySender sender = getSender(config, 100);
        sender.writeMessage("a");
        sender.stop();
        return true;
    }


    private static NettySender getSender(String config) {
        return getSender(config, 50);
    }

    private static NettySender getSender(String config, int waitRound) {
        String[] splits = config.split(":");
        NettySender sender = new NettySender(true, config);
        sender.config(splits[0], Integer.parseInt(splits[1]));
        sender.start();

        int waitCount = 0;
        while (!sender.isReady()) {
            try {
                Thread.sleep(1000);
                waitCount += 1;
                if (waitCount > waitRound) {
                    throw new RuntimeException("Failed to connect to " + config + " after " + waitRound + " attempts.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Wait for connection: " + config);
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
