package com.csc108.enginebtc.commons;

import com.csc108.enginebtc.utils.TimeUtils;

/**
 * Created by LI JT on 2019/9/9.
 * Description: Client order
 *
 *
 * 2017-07-17 13:00:00.000
 */
public class Order {

    private String orderId;

    private String stockId;
    private String symbol;
    private String origStartTime;   // 2017-07-17 15:00:00.000
    private String origEndTime;

    private String startTime;
    private String endTime;


    private String strategy;
    private String priceType;
    private double limitPx;
    private double pov;


    public Order(String orderId, String stockId, String symbol, String origStartTime, String origEndTime, String strategy, String priceType, double limitPx, double pov) {
        this.stockId = stockId;
        this.symbol = symbol;
        this.origStartTime = origStartTime;
        this.origEndTime = origEndTime;

        this.startTime = TimeUtils.shiftPmOrderTime(origStartTime);
        this.endTime = TimeUtils.shiftPmOrderTime(origEndTime);

        this.strategy = strategy;
        this.priceType = priceType;
        this.limitPx = limitPx;
        this.pov = pov;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStockId() {
        return stockId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getStartTime() {
        return this.startTime;
    }
}
