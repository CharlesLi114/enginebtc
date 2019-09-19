package com.csc108.enginebtc.replay;

import com.csc108.enginebtc.cache.TdbDataCache;
import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.controller.Controller;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.utils.TimeUtils;
import com.sun.javafx.binding.StringFormatter;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LI JT on 2019/9/9.
 * Description:
 */
public class ReplayController extends AbstractLifeCircleBean {


    private static final Logger logger = LoggerFactory.getLogger(ReplayController.class);

    public static ReplayController Replayer = new ReplayController();


    private TdbDataCache cache = TdbDataCache.TdbCache;
    private int minTimeStamp = 0;
    private int speed = 0;
    private int stepInMillis = 0;
    private long triggerInMillis = 0;

    private Map<String, Integer> timeStamps = new ConcurrentHashMap<>();
    private Scheduler scheduler;


    private ReplayController() {
        try {
            this.scheduler = StdSchedulerFactory.getDefaultScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            logger.error("Error during starting scheduler for ReplayController.");
            throw new InitializationException("Error during starting scheduler for ReplayController.", e);
        }
    }


    public void init(int timeStamp, int speed, int stepInMillis) {
        this.minTimeStamp = timeStamp;
        this.speed = speed;
        this.stepInMillis = stepInMillis;
        this.triggerInMillis = stepInMillis / speed;
        if (stepInMillis % speed != 0) {
            throw new InitializationException(MessageFormat.format("stepInMillis {0} is not a multiple of speed {1}, which is not allowed.", stepInMillis, speed));
        }

        Set<String> stocksIds = cache.stockIds();
        for (String stockid : stocksIds) {
            this.timeStamps.put(stockid, minTimeStamp);
        }
    }


    @Override
    public void config() {

    }

    @Override
    public void start() {

        cache.initCursor(this.minTimeStamp);

        for (String stockId : cache.stockIds()) {
            JobDetail job = JobBuilder.newJob(ReplayJob.class).usingJobData("StockId", stockId).usingJobData("Step", this.stepInMillis).build();
            Trigger trigger = TriggerBuilder.newTrigger().startNow().withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(this.triggerInMillis).repeatForever()).build();
            try {
                scheduler.scheduleJob(job, trigger);
            } catch (SchedulerException e) {
                logger.error("Error when creating quartz job.", e);
                throw new InitializationException("Error when creating quartz job.", e);
            }
        }
    }

    @Override
    public void stop() {
        if (this.scheduler != null) {
            try {
                this.scheduler.shutdown();
            } catch (SchedulerException e) {
                logger.error("Exception during closing scheduler.", e);
            }
        }
    }


    public int getLastUpto(String stockId) {
        return this.timeStamps.get(stockId);
    }

    public void updateUpto(String stockId, int upto) {
        this.timeStamps.put(stockId, upto);
    }
}
