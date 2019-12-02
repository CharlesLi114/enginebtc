package com.csc108.enginebtc.cache;

import cn.com.wind.td.tdb.TickAB;
import cn.com.wind.td.tdb.Transaction;
import com.csc108.enginebtc.amq.ActiveMqController;
import com.csc108.enginebtc.tdb.TdbController;
import com.csc108.enginebtc.tdb.models.MarketData;
import com.csc108.enginebtc.tdb.models.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by LI JT on 2019/9/9.
 * Description:
 */
public class TdbDataCache {

    private static final Logger logger = LoggerFactory.getLogger(TdbDataCache.class);


    public static TdbDataCache TdbCache = new TdbDataCache();


    private Map<String, TdbDataList<TransactionData>> transactionCache;
    private Map<String, TdbDataList<MarketData>> marketdataCache;
    private Set<String> stockIds;

    private Map<String, Integer> timeStamps = new ConcurrentHashMap<>();

    private AtomicInteger tickSentRound = new AtomicInteger(0);
    private AtomicInteger tradeSentRound = new AtomicInteger(0);
    private String watchStock;


    private TdbDataCache() {
        this.marketdataCache = new HashMap<>();
        this.transactionCache = new HashMap<>();
    }

    public void readTdb(List<String> stockIds, int date) {
        int i = 1;
        for (String stockId : stockIds) {
            logger.info("Reading " + stockId + ", " + i++ + "/" + stockIds.size());
            this.addMarketData(stockId, date);
            this.addTransaction(stockId, date);
        }
        this.stockIds = new HashSet<>(stockIds);

        int maxLength = 0;
        for (Map.Entry<String, TdbDataList<TransactionData>> entry : transactionCache.entrySet()) {
            if (entry.getValue().size() > maxLength) {
                this.watchStock = entry.getKey();
                maxLength = entry.getValue().size();
            }
        }
    }

    private void addMarketData(String stockId, int date) {
        TickAB[] ticks = TdbController.TdbController.getTick(stockId, date);
        if (ticks == null || ticks.length == 0) {
            logger.warn(MessageFormat.format("Empty ticks for {0} of date {1}.", stockId, date));
            return;
        }

        TdbDataList<MarketData> l = new TdbDataList<>(stockId);
        List<MarketData> datas = Arrays.stream(ticks).map(MarketData::new).collect(Collectors.toList());

        // Change turnover and volume of MarketData to cumulative value.
        if (datas.size() > 1) {
            for(int i = 1; i < datas.size(); i++) {
                datas.get(i).cumValues(datas.get(i-1));
            }
        }


        l.setData(datas);
        marketdataCache.put(stockId, l);
    }

    private void addTransaction(String stockId, int date) {
        Transaction[] transactions = TdbController.TdbController.getTransaction(stockId, date);
        if (transactions == null || transactions.length == 0) {
            logger.warn(MessageFormat.format("Empty trades for {0} of date {1}.", stockId, date));
            return;
        }

        TdbDataList<TransactionData> l = new TdbDataList<>(stockId);
        List<TransactionData> datas = Arrays.stream(transactions).map(TransactionData::new).collect(Collectors.toList());
        l.setData(datas);
        transactionCache.put(stockId, l);
    }

    /**
     * Initialize timestamp,
     * @param timeStamp of pattern 91500000, which means the earliest timestamp of all incoming orders.
     */
    public void initCursor(int timeStamp) {
        for (TdbDataList l : transactionCache.values()) {
            l.initCursor(timeStamp);
        }
        for (TdbDataList l : marketdataCache.values()) {
            l.initCursor(timeStamp);
        }
    }


    private List<MarketData> getTdbMarketData(String stockId, int upto) {
        if (stockId.equalsIgnoreCase(watchStock)) {
            tickSentRound.incrementAndGet();
        }

        if (this.marketdataCache.keySet().contains(stockId)) {
            return this.marketdataCache.get(stockId).getData(upto);
        } else {
            return null;
        }
    }


    private List<TransactionData> getTdbTransactionData(String stockId, int upto) {
        if (stockId.equalsIgnoreCase(watchStock)) {
            tradeSentRound.incrementAndGet();
        }

        if (this.transactionCache.keySet().contains(stockId)) {
            return this.transactionCache.get(stockId).getData(upto);
        } else {
            return null;
        }
    }

    public Set<String> getStockIds() {
        return this.stockIds;
    }

    public void publishTicks(String stockId, int upto) {
        List<MarketData> datas = this.getTdbMarketData(stockId, upto);
        if (datas == null || datas.isEmpty()) {
            return;
        }
        for (MarketData data : datas) {
            ActiveMqController.Controller.sendTicks(data);
        }
    }

    public void publishTrades(String stockId, int upto) {
        List<TransactionData> datas = TdbDataCache.TdbCache.getTdbTransactionData(stockId, upto);
        if (datas == null || datas.isEmpty()) {
            return;
        }
        for (TransactionData data : datas) {
            ActiveMqController.Controller.sendTrans(data);
        }
    }

    public String getWatchStock() {
        return this.watchStock;
    }

    public int getTradeTriggerTimes() {
        return this.tradeSentRound.get();
    }

    public int getTicksTriggerTimes() {
        return this.tickSentRound.get();
    }


}
