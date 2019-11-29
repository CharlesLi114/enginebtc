package com.csc108.enginebtc.replay;

import com.csc108.enginebtc.cache.TdbDataCache;
import com.csc108.enginebtc.utils.TimeUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Created by LI JT on 2019/11/29.
 * Description: Watch if replay process is going as expected.
 */
public class MonitorJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(TdbDataCache.class);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     * </p>
     * <p>
     * <p>
     * The implementation may wish to set a
     * {@link JobExecutionContext#setResult(Object) result} object on the
     * {@link JobExecutionContext} before this method exits.  The result itself
     * is meaningless to Quartz, but may be informative to
     * <code>{@link JobListener}s</code> or
     * <code>{@link TriggerListener}s</code> that are watching the job's
     * execution.
     * </p>
     *
     * @param context
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        StringBuilder sb = new StringBuilder();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        int initialUpTo = dataMap.getInt("InitialSyncTo");
        int stepInMillis = dataMap.getInt("Step");
        String watchStock = TdbDataCache.TdbCache.getWatchStock();
        int actualTime = ReplayController.Replayer.getLastUpto(watchStock);

        int triggerTimes = TdbDataCache.TdbCache.getTradeTriggerTimes();
        int timePassedInMillis = stepInMillis * triggerTimes;
        int expectedTime = TimeUtils.addMiliis(initialUpTo, timePassedInMillis);
        sb.append("\n").append(MessageFormat.format("  Stock {0} trade send: expected upto {1}, actual upto {1}.\n", watchStock, TimeUtils.tsToOutputString(expectedTime), TimeUtils.tsToOutputString(actualTime)));

        triggerTimes = TdbDataCache.TdbCache.getTicksTriggerTimes();
        timePassedInMillis = stepInMillis * triggerTimes;
        expectedTime = TimeUtils.addMiliis(initialUpTo, timePassedInMillis);
        sb.append(MessageFormat.format("  Stock {0} ticks send: expected upto {1}, actual upto {1}", watchStock, TimeUtils.tsToOutputString(expectedTime), TimeUtils.tsToOutputString(actualTime)));

        logger.info(sb.toString());
    }
}
