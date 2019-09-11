package com.csc108.enginebtc;

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

    public static void main(String[] args) throws SchedulerException {

//        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
//        Scheduler scheduler = schedulerFactory.getScheduler();
//
//
//        JobDetail job = JobBuilder.newJob(SendingJob.class).usingJobData("StockId", "1231").build();
//        Trigger trigger = TriggerBuilder.newTrigger().startNow().withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(1000).repeatForever()).build();
//
//        scheduler.start();
//        scheduler.scheduleJob(job, trigger);



    }
}
