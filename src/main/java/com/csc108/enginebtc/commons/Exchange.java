package com.csc108.enginebtc.commons;

import com.csc108.enginebtc.cache.RepoCodeCache;

/**
 * Created by LI JT on 2019/9/6.
 * Description:
 */
public enum Exchange {

    SH,


    SZ,


    RPSH,


    RPSZ;


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



}
