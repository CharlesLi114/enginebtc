package com.csc108.enginebtc;

import com.csc108.enginebtc.admin.NettySender;
import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.replay.ReplayController;
import com.csc108.enginebtc.utils.TimeUtils;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static void main(String[] args) throws SchedulerException, InterruptedException, JMSException {

        System.out.println(TimeUtils.getTimeStamp());


        ActiveMQMapMessage msg = new ActiveMQMapMessage();
        double[] array = {10.0, 10.0, 10.0};
        List<Double> l = new ArrayList<Double>();
        l.add(10.0);
        l.add(10.9);
        msg.setObject("T", l);


        TimeUtils.shiftOrderPmTime("2019-11-13 10:25:05.490");


        OrderCache cache = OrderCache.OrderCache;
        cache.start();



    }
}
