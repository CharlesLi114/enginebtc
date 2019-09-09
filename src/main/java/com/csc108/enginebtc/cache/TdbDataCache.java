package com.csc108.enginebtc.cache;

import cn.com.wind.td.tdb.Tick;
import cn.com.wind.td.tdb.Transaction;
import com.csc108.enginebtc.tdb.models.MarketData;
import com.csc108.enginebtc.tdb.models.TransactionData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by LI JT on 2019/9/9.
 * Description:
 */
public class TdbDataCache {

    public static TdbDataCache TdbCache = new TdbDataCache();


    private Map<String, TdbDataList<TransactionData>> transactionCache;
    private Map<String, TdbDataList<MarketData>> marketdataCache;


    private TdbDataCache() {
        this.marketdataCache = new HashMap<>();
        this.transactionCache = new HashMap<>();
    }

    public void addMarketData(String stockId, Tick[] ticks) {
        TdbDataList<MarketData> l = new TdbDataList<>(stockId);
        List<MarketData> datas = Arrays.stream(ticks).map(MarketData::new).collect(Collectors.toList());
        l.setData(datas);
        marketdataCache.put(stockId, l);
    }

    public void addTransaction(String stockId, Transaction[] transactions) {
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
        if (this.marketdataCache.keySet().contains(stockId)) {
            return this.marketdataCache.get(stockId).getData(upto);
        } else {
            return null;
        }
    }

    public List<TransactionData> getTdbTransactioData(String stockId, int upto) {
        if (this.transactionCache.keySet().contains(stockId)) {
            return this.transactionCache.get(stockId).getData(upto);
        } else {
            return null;
        }
    }
}
