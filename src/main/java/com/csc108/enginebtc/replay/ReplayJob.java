package com.csc108.enginebtc.replay;

import com.csc108.enginebtc.cache.TdbDataCache;
import com.csc108.enginebtc.utils.TimeUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by LI JT on 2019/9/10.
 * Description:
 */
public class ReplayJob implements Job {
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
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String stockId = dataMap.getString("StockId");
        int stepInMillis = dataMap.getInt("Step");
        int lastUpto = ReplayController.Replayer.getLastUpto(stockId);
        int upto = TimeUtils.addMiliis(lastUpto, stepInMillis);
        TdbDataCache.TdbCache.publishTicks(stockId, upto);
        TdbDataCache.TdbCache.publishTrades(stockId, upto);
        ReplayController.Replayer.updateUpto(stockId, upto);
    }
}
