package com.csc108.enginebtc;

import com.csc108.enginebtc.admin.NettySender;
import com.csc108.enginebtc.cache.OrderCache;
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

        TimeUtils.shiftOrderPmTime("2019-11-13 10:25:05.490");


        OrderCache cache = OrderCache.OrderCache;
        cache.start();



    }
}
