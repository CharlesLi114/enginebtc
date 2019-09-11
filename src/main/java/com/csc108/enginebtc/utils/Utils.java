package com.csc108.enginebtc.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by LI JT on 2019/9/11.
 * Description:
 */
public class Utils {

    public static String getSymbol(String stockId) {
        return StringUtils.substringBefore(stockId, ".");
    }

    public static String getExchange(String stockId) {
        return StringUtils.substringAfter(stockId, ".");
    }



}
