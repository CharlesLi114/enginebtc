package com.csc108.enginebtc;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * This application schedule a job to run every minute
 *
 * @author Mary.Zheng
 *
 */




public class QuartzTest {

    public static void main(String[] args) {
        QuartzTest quartzTest=new QuartzTest();
        quartzTest.startSchedule();

    }

    public void print() {
        System.out.println("Hello quzrtz  " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date()));
    }


    public void startSchedule() {
        try {
            // 1、创建一个JobDetail实例，指定Quartz
            JobDetail jobDetail = JobBuilder.newJob(Quartz1.class)
                    // 任务执行类
                    .withIdentity("job1_1", "jGroup1")
                    // 任务名，任务组
                    .build();
            //2、创建Trigger
            SimpleScheduleBuilder builder = SimpleScheduleBuilder.simpleSchedule()
                    //设置间隔执行时间
                    .withIntervalInSeconds(5)
                    //设置执行次数
                    .repeatForever();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(
                    "trigger1_1", "tGroup1").startNow().withSchedule(builder).build();
            //3、创建Scheduler
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            //4、调度执行
            scheduler.scheduleJob(jobDetail, trigger);
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            scheduler.shutdown();

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public static class Quartz1 implements Job {
        /**
         * 事件类，处理具体的业务
         */
        @Override
        public void execute(JobExecutionContext arg0) throws JobExecutionException {

        }
    }
}