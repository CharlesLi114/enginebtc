package com.csc108.enginebtc.cache;

import cn.com.wind.td.tdb.TickAB;
import cn.com.wind.td.tdb.Transaction;
import com.csc108.enginebtc.tdb.TdbController;
import com.csc108.enginebtc.tdb.models.MarketData;
import com.csc108.enginebtc.tdb.models.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private long tickSentRound = 0;
    private long tradeSentRound = 0;
    private String watchStock;


    private TdbDataCache() {
        this.marketdataCache = new HashMap<>();
        this.transactionCache = new HashMap<>();
    }

    public void readTdb(List<String> stockIds, int date) {
        int i = 0;
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


    public List<MarketData> getTdbMarketData(String stockId, int upto) {
        if (stockId.equalsIgnoreCase(watchStock)) {
            tickSentRound += 1;
            // Log it every 3000 rounds, by default with 10 ms interval, it is 30 seconds.
            if (tickSentRound % 3000 == 0) {
                logger.info("Stock " + this.watchStock + " ticks sends upto " + upto);
            }
        }

        if (this.marketdataCache.keySet().contains(stockId)) {
            return this.marketdataCache.get(stockId).getData(upto);
        } else {
            return null;
        }
    }

    public List<TransactionData> getTdbTransactionData(String stockId, int upto) {
        if (stockId.equalsIgnoreCase(watchStock)) {
            tradeSentRound += 1;
            if (tradeSentRound % 3000 == 0) {
                logger.info("Stock " + this.watchStock + " trades sends upto " + upto);
            }
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


}
