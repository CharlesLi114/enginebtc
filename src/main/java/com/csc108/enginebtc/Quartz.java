package com.csc108.enginebtc;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by LI JT on 2019/9/10.
 * Description:
 */

public class Quartz implements Job {
    /**
     * 事件类，处理具体的业务
     */
    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        System.out.println("Hello quzrtz  " +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(new Date()));
    }
}
