package com.csc108.enginebtc;

import com.csc108.enginebtc.admin.NettySender;
import com.csc108.enginebtc.replay.ReplayController;
import com.csc108.enginebtc.utils.TimeUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class Test {



    public class SendingJob implements Job {
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            String stockId = dataMap.getString("StockId");
            System.out.println(stockId);
        }
    }

    public class HelloJob implements Job
    {
        public void execute(JobExecutionContext context)
                throws JobExecutionException {

            System.out.println("Hello Quartz!");

        }

    }

    public static void main(String[] args) throws SchedulerException, InterruptedException {

        NettySender sender = new NettySender(true);
        sender.config("10.101.237.68", 9201);
        sender.start();
        while (!sender.isReady()) {
            Thread.sleep(1000);
            System.out.println("Wait for connection.");
        }
        sender.writeMessage("data amq list");

        Thread.sleep(10000);
        sender.stop();

        sender = new NettySender(true);
        sender.config("10.101.237.68", 9201);
        sender.start();
        while (!sender.isReady()) {
            Thread.sleep(1000);
            System.out.println("Wait for connection.");
        }
        sender.writeMessage("data amq list");
        Thread.sleep(10000);
        sender.stop();




    }
}
