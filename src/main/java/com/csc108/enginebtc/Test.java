package com.csc108.enginebtc;

import com.csc108.enginebtc.admin.NettySender;
import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.replay.ReplayController;
import com.csc108.enginebtc.utils.TimeUtils;
import com.csc108.enginebtc.utils.Utils;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.jms.JMSException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
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

    /**
     *
     * @param args
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws JMSException
     * @throws IOException
     */
    public static void main(String[] args) throws SchedulerException, InterruptedException, JMSException, IOException {

        System.out.println(TimeUtils.getTimeStamp());

//        String command = "D:\\projects\\J\\enginebtc\\src\\main\\python\\sync_for_enginebtc\\py\\exec_sync.bat" + " " + 20191113;

//        Process process = Runtime.getRuntime().exec(new String[] {"cmd.exe","/c",command});
//
//        BufferedReader stdInput = new BufferedReader(new
//                InputStreamReader(process.getInputStream()));
//
//        BufferedReader stdError = new BufferedReader(new
//                InputStreamReader(process.getErrorStream()));
//
//        // Read the output from the command
//        System.out.println("Here is the standard output of the command:\n");
//        String s = null;
//        while ((s = stdInput.readLine()) != null) {
//            System.out.println(s);
//        }
//
//
////        process.waitFor();
//        int exitValue = process.exitValue();


        String command = "D:\\projects\\J\\enginebtc\\src\\main\\python\\sync_for_enginebtc\\py\\exec_sync.bat";
        String cmdArgs = "20191113";
        System.out.println(Utils.execBatch(command, cmdArgs));


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
