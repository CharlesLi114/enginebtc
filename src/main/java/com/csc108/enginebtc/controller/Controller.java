package com.csc108.enginebtc.controller;

import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.exception.InvalidParamException;
import com.csc108.enginebtc.replay.ReplayController;
import com.csc108.enginebtc.utils.ConfigUtil;
import com.csc108.enginebtc.utils.SyncUtils;
import com.csc108.enginebtc.utils.TimeUtils;
import com.csc108.enginebtc.utils.Utils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by LI JT on 2019/9/2.
 * Description: This whole class can be merged to Application.
 */
public class Controller extends AbstractLifeCircleBean {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private static final String PROPERTY_CONFIG_FILE = "application.properties";
    private static final String Speed_Property_Name = "sys.speed";
    private static final String StepInMillis_Property_Name = "sys.step.millis";
    private static final String WarmupSecs_Property_Name = "sys.warmup.secs";

    private static final String EngineConfig_Property_Name = "engine.admin.port";
    private static final String CalcConfig_Property_Name = "calc.admin.port";

    private static final String Sync_Batch_Property_Name = "sync.batch.file";
    private static final String Comp_Executor_Property_Name = "comps.executor.files";

    public static Controller Controller = new Controller();


    private volatile boolean isSystemReady = false;

//    private volatile boolean isCalcDataReady = false;
//    private volatile boolean isCalcTimeSet = false;
//    private volatile boolean isEngineTimeSet = false;


    private AtomicInteger calcTimeSetCount = new AtomicInteger(0);
    private AtomicInteger engTimeSetCount = new AtomicInteger(0);
    private AtomicInteger calcDataReadyCount = new AtomicInteger(0);    // Could use one variable. Use a count for possible multiple calcs.

    private int speed;
    private int warmupSecs;
    private int stepInMillis;
    private int minTimeStamp = -1;

    private List<String> engines;
    private List<String> calcs;

    private int tsBfSync = 0;   // TimeStamp before send time sync operation.
    private int tsAfterSync = 0;


    private Controller() {
        engines = new ArrayList<>();
        calcs = new ArrayList<>();
        this.config();
    }

    @Override
    public void config() {
        String configFile = ConfigUtil.getConfigPath(PROPERTY_CONFIG_FILE);
        try {
            Configuration config = new PropertiesConfiguration(configFile);
            this.speed = config.getInt(Speed_Property_Name);
            this.warmupSecs = config.getInt(WarmupSecs_Property_Name);
            this.stepInMillis = config.getInt(StepInMillis_Property_Name);

            if (this.speed != 1) {
                throw new InvalidParamException("Current not support speed other than 1.");
            }

            logger.info("Using config ");
            logger.info("Speed : " + speed);
            logger.info("WarmupSecs : " + warmupSecs);
            logger.info("StepInMillis : " + stepInMillis);


            List<Object> engines = config.getList(EngineConfig_Property_Name);
            for (Object o : engines) {
                this.engines.add((String) o);
            }


            List<Object> calcs = config.getList(CalcConfig_Property_Name);
            for (Object o : calcs) {
                this.calcs.add((String) o);
            }

        } catch (ConfigurationException e) {
            logger.error("Failed to read config for system config.", e);
            throw new InitializationException("Failed to read config for system config", e);
        }
    }


    /**
     *
     */
    @Override
    public void start() {
        OrderCache.OrderCache.publishOrders();
        if (this.minTimeStamp == -1) {
            int orderMinTimeStamp = OrderCache.OrderCache.getMinTimestamp();
            this.minTimeStamp = TimeUtils.addSeconds(orderMinTimeStamp, -warmupSecs);
        }

        Calendar c1 = TimeUtils.getCalender(this.tsBfSync);
        Calendar c2 = TimeUtils.getCalender(this.tsAfterSync);

        long syncTimeInMillis = c2.getTimeInMillis() - c1.getTimeInMillis();
        int initialSyncTo = TimeUtils.addMiliis(this.minTimeStamp, (int)syncTimeInMillis);

        ReplayController.Replayer.init(this.minTimeStamp, speed, stepInMillis, initialSyncTo);
        ReplayController.Replayer.start();
    }


    /**
     * Set offset time for engine(s) and calc(s).
     * {@link Controller#tsBfSync} and {@link Controller#tsAfterSync} are used to record timestamp before and after set offset.
     * The time between is then compensated in {@link Controller#start()}.
     */
    public void setTimeOffset() {
        int orderMinTimeStamp = OrderCache.OrderCache.getMinTimestamp();
        this.minTimeStamp = TimeUtils.addSeconds(orderMinTimeStamp, -warmupSecs);

        this.tsBfSync = TimeUtils.getTimeStamp();
        SyncUtils.syncWithEngine(minTimeStamp, engines);
        SyncUtils.syncWithCalc(minTimeStamp, calcs, speed);
        this.waitForTimeSynced(0);
        this.tsAfterSync = TimeUtils.getTimeStamp();
    }


    /**
     *
     */
    public void setCalcTimeOffset() {
        if (this.minTimeStamp == -1) {
            int orderMinTimeStamp = OrderCache.OrderCache.getMinTimestamp();
            this.minTimeStamp = TimeUtils.addSeconds(orderMinTimeStamp, -warmupSecs);
        }
        this.tsBfSync = TimeUtils.getTimeStamp();
        SyncUtils.syncWithCalc(minTimeStamp, calcs, speed);
        this.waitForTimeSynced(2);
        this.tsAfterSync = TimeUtils.getTimeStamp();
    }

    /**
     *
     */
    public void syncStockCodeWithCalc() {
        Thread thread = new Thread() {
            public void run() {
                logger.info("Sync stock code with calc.");
                OrderCache cache = OrderCache.OrderCache;
//                SyncUtils.syncStocksWithCalc(calcs, cache.getStockIds(), cache.getDate(), cache.getMinTimestamp());
                try {
                    SyncUtils.syncStockWithCalc(cache.getStockIds(), cache.getDate(), cache.getMinTimestamp());
                } catch (JMSException e) {
                    throw new RuntimeException("Failed to send stock codes to calcs.");
                }
            }
        };
        thread.start();
    }


    /**
     * Wait until calc finished reading tdf data.
     */
    public void waitForCalcDataReady() {
        while (this.calcDataReadyCount.get() < this.calcs.size()) {
            logger.info("Wait for calc to be ready.");
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                throw new InitializationException("Failed to wait for system ready signal.", e);
            }
        }
    }


    /**
     * Add one data-ready calc.
     */
    public void addDataReadyCalc() {
        this.calcDataReadyCount.incrementAndGet();
    }

    public void addTimeReadyCalc() {
        this.calcTimeSetCount.getAndIncrement();
    }

    public void setEngineTimeReady() {
        this.engTimeSetCount.getAndIncrement();
    }


    /**
     * Wait until Calc and Engine are ready, with time drift applied.
     * Calc should send message back saying it has processed the data.
     */
    private void waitForTimeSynced(int option) {
        while (!isReady(option)) {
            logger.info("Wait for time offset to be ready.");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new InitializationException("Failed to wait for system ready signal.", e);
            }
        }
    }


    private boolean isReady(int option) {
        if (option == 0) {
            return this.engTimeSetCount.get() == this.engines.size() && this.calcTimeSetCount.get() == this.calcs.size();
        } else if (option == 1) {
            return this.engTimeSetCount.get() == this.engines.size();
        } else if (option == 2){
            return this.calcTimeSetCount.get() == this.calcs.size();
        } else {
            throw new UnsupportedOperationException("Input option code " + option + " is not supported.");
        }
    }

    public List<String> getCalcs() {
        return this.calcs;
    }

    public List<String> getEngines() {
        return this.engines;
    }


    /**
     * Wait until calc and engine are ready, by connecting to their communication port.
     *
     */
    public void waitCompsStarted() throws Exception {
        List<String> l = new ArrayList<>();
        l.addAll(engines);
        l.addAll(calcs);
        for (String config : l) {
            logger.info("Wait for " + config);
            String[] splits = config.split(":");
            boolean available = false;
            for (int i = 0; i < 10; i++) {
                available = SyncUtils.available(splits[0], Integer.valueOf(splits[1]));
                if (!available) {
                    logger.warn(config + " " + (i+1) + " attempts, not available.");
                    Thread.sleep(1000 * 5);
                } else {
                    break;
                }
            }
            if (!available) {
                logger.error("Failed to connect to " + config);
                throw new Exception("Failed to connect to " + config);
            } else {
                logger.info(config + " connected.");
            }
        }
    }


    @Override
    public void stop() {
        ReplayController.Replayer.stop();
    }

    public boolean isSystemReady() {
        return this.isSystemReady;
    }


    public void setSystemReady() {
        this.isSystemReady = true;
    }


    /**
     * This method call python sync method.
     * If {@code srcDate == 0} meaning there is no input date, and default mode is used, which calls {@link OrderCache#getDate()} to get order date.
     *
     *
     * @param srcDate is the date of the orders. Remember this is not the microstructure date, it is handled in sync_for_enginebtc project.
     * @throws ConfigurationException
     * @throws IOException
     */
    public void syncData(int srcDate) throws ConfigurationException, IOException {
        logger.info("Running data sync job.");

        String configFile = ConfigUtil.getConfigPath(PROPERTY_CONFIG_FILE);
        Configuration config = new PropertiesConfiguration(configFile);
        String batchFile = config.getString(Sync_Batch_Property_Name);

        if (srcDate == 0) {
            OrderCache.OrderCache.start();
            srcDate = OrderCache.OrderCache.getDate();
        }
        logger.info("   BatchFile: " + batchFile);
        logger.info("   Date: " + srcDate);

        int exitVal = Utils.execBatch(batchFile, String.valueOf(srcDate));
        logger.info("Job execution with return code " + exitVal);
    }

    public void startComponents() throws ConfigurationException, IOException {
        String configFile = ConfigUtil.getConfigPath(PROPERTY_CONFIG_FILE);
        Configuration config = new PropertiesConfiguration(configFile);
        List<Object> comps = config.getList(Comp_Executor_Property_Name);
        for (Object o : comps) {
            System.out.println("File: " + o);
            Utils.startJob(String.valueOf(o), null);
        }
    }


}

