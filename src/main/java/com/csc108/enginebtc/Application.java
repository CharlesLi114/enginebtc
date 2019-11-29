package com.csc108.enginebtc;

import com.csc108.enginebtc.amq.ActiveMqController;
import com.csc108.enginebtc.cache.FixSessionCache;
import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.cache.TdbDataCache;
import com.csc108.enginebtc.controller.Controller;
import com.csc108.enginebtc.fix.InitiatorApp;
import com.csc108.enginebtc.tdb.TdbController;
import com.csc108.enginebtc.utils.ConfigUtil;
import com.csc108.enginebtc.utils.FileUtils;
import com.csc108.enginebtc.utils.TimeUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 *
 *
 * TODO:
 * 2. If market data has no bidprice array or volume array, how to handle it? Now an empty is sent, but matcher doest not check it.
 *
 * 4. Remove randomness in engine decisions.
 */
public class Application {

    static {
        String logHome = System.getProperty("loghome");
        if (null == logHome) {
            logHome = "D:/logs/BackTestSuit/";
        }
        System.setProperty("log.home", logHome + TimeUtils.getActionDay() + "/controller");

        String configFile = ConfigUtil.getConfigPath("initiator.cfg.tpl");
        try {
            Configuration config = new PropertiesConfiguration(configFile);
            String tplFile = ((PropertiesConfiguration) config).getURL().getFile();
            String newFile = tplFile.replace(".tpl", "");
            IOUtils.copy(new FileInputStream(tplFile), new FileOutputStream(newFile));

            FileUtils.replaceFileWithReg(newFile, "\\$\\{LogBaseDir}", logHome + TimeUtils.getActionDay());
        } catch (ConfigurationException | IOException e) {
            e.printStackTrace();
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            System.exit(-1);
        }
    }



    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static Application Application = new Application();

    private static ThreadedSocketInitiator OmInitiator;
    private static InitiatorApp omInitiatorApplication;



    private static void startInitiator () throws Exception{
        String configFile = ConfigUtil.getConfigPath("initiator.cfg");
//        SessionSettings settings = new SessionSettings("configuration/initiator.cfg");
        SessionSettings settings = new SessionSettings(configFile);
        FileStoreFactory fileStoreFactory = new FileStoreFactory(settings);
        FileLogFactory logFactory = new FileLogFactory(settings);
        DefaultMessageFactory defaultMessageFactory = new DefaultMessageFactory();
        omInitiatorApplication = new InitiatorApp();
        OmInitiator = new ThreadedSocketInitiator(omInitiatorApplication,fileStoreFactory,settings, logFactory,defaultMessageFactory);
        OmInitiator.start();
        FixSessionCache.getInstance().setExpectedSessionNb(OmInitiator.getSessions().size());
    }

    /**
     * Main work flow.
     * @throws Exception
     */
    public void work() throws Exception {
        OrderCache.OrderCache.start();
        ActiveMqController.Controller.start();
        TdbController.TdbController.start();
        startInitiator();

        Controller.Controller.waitCompsStarted();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Controller.Controller.syncStockCodeWithCalc();
        TdbDataCache.TdbCache.readTdb(OrderCache.OrderCache.getStockIds(), OrderCache.OrderCache.getDate());
        Controller.Controller.waitForCalcDataReady();

        Controller.Controller.publishOrders();
        Controller.Controller.start();
        this.waitToClose();
        OrderCache.OrderCache.cancelOrders();   // Cancel all existing for a proper exit.

        OmInitiator.stop();
        System.exit(0);
    }


    /**
     * Test to send data to mq, for others test use.
     */
    public void testSendDataToMq() {
        ActiveMqController.Controller.start();

        TdbController.TdbController.start();
        OrderCache.OrderCache.start();
        Controller.Controller.syncStockCodeWithCalc();
        TdbDataCache.TdbCache.readTdb(OrderCache.OrderCache.getStockIds(), OrderCache.OrderCache.getDate());


        Controller.Controller.waitForCalcDataReady();
        Controller.Controller.syncWithCalc();


        Controller.Controller.start();
        this.waitToClose();
    }

    /**
     * Test to send send order to downstream.
     */
    public void testPublishOrders() throws Exception {
        OrderCache.OrderCache.start();
        startInitiator();

        while (!FixSessionCache.getInstance().isFixSessionReady()) {
            logger.info("Waiting for Fix Sessions to connect.");
            Thread.sleep(1000);
        }
        logger.info("Fix sessions ready.");
        OrderCache.OrderCache.publishOrders();
        while (true) {
            Thread.sleep(10000);
        }
    }

    public void waitToClose() {
        Thread thread = new Thread() {
            public void run() {
            while (true) {
                Scanner sc = new Scanner(System.in);
                String input = sc.next();
                if (input.equalsIgnoreCase("STOP")) {
                    logger.info("STOP request received, will shut down.");
                    break;
                } else {
                    System.out.print("Type STOP to shut down.");
                }
            }
            }
        };
        thread.run();
    }


    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Application.testSendDataToMq();


        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("-sync")) {
                if (args.length == 2) {
                    String srcDate = args[1];
                    Controller.Controller.syncData(Integer.valueOf(srcDate));
                } else {
                    Controller.Controller.syncData(0);
                }
            } else if (args[0].equalsIgnoreCase("-exec")) {
                Controller.Controller.startComponents();
            }
        } else {
            Application.work();
        }

//        Application.testSendDataToMq();




    }



}
