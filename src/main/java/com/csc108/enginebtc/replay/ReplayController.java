package com.csc108.enginebtc.replay;

import com.csc108.enginebtc.cache.TdbDataCache;
import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.exception.InitializationException;
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
    private int ordMinTimestamp = 0;
    private int speed = 0;
    private int stepInMillis = 0;
    private long triggerInMillis = 0;
    private int initialSyncTo = 0;

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


    /**
     *  If speed == 1, will trigger once after stepInMillis; if speed == 2, will trigger once after stepInMillis/2.
     *  For instance, will move forward 10 millis after 10 millis when speed == 1; or when speed == 2, will move forward 10 millis every 5 millis.
     * @param timeStamp
     * @param speed
     * @param stepInMillis
     */
    public void init(int timeStamp, int speed, int stepInMillis, int initialSyncTo) {
        this.ordMinTimestamp = timeStamp;
        this.speed = speed;
        this.stepInMillis = stepInMillis;
        this.triggerInMillis = stepInMillis / speed;
        if (stepInMillis % speed != 0) {
            throw new InitializationException(MessageFormat.format("stepInMillis {0} is not a multiple of speed {1}, which is not allowed.", stepInMillis, speed));
        }

        Set<String> stocksIds = cache.getStockIds();
        for (String stockid : stocksIds) {
            this.timeStamps.put(stockid, ordMinTimestamp);
        }
    }


    @Override
    public void config() {

    }

    @Override
    public void start() {
        logger.info("Initialize order-min-timestamp to  : " + this.ordMinTimestamp);
        cache.initCursor(this.ordMinTimestamp);

        logger.info("Shift in sync time: " + initialSyncTo + " for system time usage compensation.");
        for (String stockId : cache.getStockIds()) {
            TdbDataCache.TdbCache.publishTicks(stockId, initialSyncTo);
            TdbDataCache.TdbCache.publishTrades(stockId, initialSyncTo);
            ReplayController.Replayer.updateUpto(stockId, initialSyncTo);
        }

        for (String stockId : cache.getStockIds()) {
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


    /**
     * Get upto where replay has reached.
     * @param stockId
     * @return
     */
    public int getLastUpto(String stockId) {
        return this.timeStamps.get(stockId);
    }

    /**
     * Update upto for one stock.
     * @param stockId
     * @param upto
     */
    public void updateUpto(String stockId, int upto) {
        this.timeStamps.put(stockId, upto);
    }



}
