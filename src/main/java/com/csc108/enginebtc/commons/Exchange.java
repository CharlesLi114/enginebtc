package com.csc108.enginebtc.commons;

import com.csc108.enginebtc.cache.RepoCodeCache;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Created by LI JT on 2019/9/6.
 * Description:
 */
public enum Exchange {

    SH("SS"),


    SZ("SZ"),


    RPSH("SS"),


    RPSZ("SZ");


    private String fixId;

    Exchange(String fixId) {
        this.fixId = fixId;
    }


    /***
     * Get exchange for shanghai and shenzhen tickers.
     * @param stockId like 000001.sz
     * @return
     */
    public static Exchange getExchange(String stockId) {
        String exchange = stockId.split(".")[1];
        if (exchange.equalsIgnoreCase("SZ")) {
            return RepoCodeCache.RepoCache.isRepo(stockId)? RPSZ: SZ;
        } else {
            return RepoCodeCache.RepoCache.isRepo(stockId)? RPSH: SH;
        }
    }

    public static Exchange parse(String exDest) {
        if (exDest.equalsIgnoreCase("SS")) {
            return SH;
        }
        if (exDest.equalsIgnoreCase("SZ")) {
            return SZ;
        }
        throw new NotImplementedException("Exchange " + exDest + " is not implemented yet.");
    }

    public String getFixId() {
        return this.fixId;
    }





}
