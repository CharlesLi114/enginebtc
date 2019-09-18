package com.csc108.enginebtc.commons;

import com.csc108.enginebtc.fix.FixTags;
import com.csc108.enginebtc.utils.Constants;
import com.csc108.enginebtc.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;

import java.util.Date;
import java.util.UUID;

/**
 * Created by LI JT on 2019/9/9.
 * Description: Client order
 *
 *
 * 2017-07-17 13:00:00.000
 */
public class Order {

    private String orderId;
    private String accountId;

    private String stockId;
    private String symbol;
    private String origStartTime;   // 2017-07-17 15:00:00.000
    private String origEndTime;

    private String startTime;
    private String endTime;

    private Exchange exchange;
    private Side side;
    private int qty;


    private String strategy;
    private OrdType ordType;
    private double limitPx;
    private double pov;


    public Order(String orderId, String stockId, String symbol, String origStartTime, String origEndTime, String strategy, String priceType, double limitPx, double pov) {
        this.stockId = stockId;
        this.symbol = symbol;
        this.origStartTime = origStartTime;
        this.origEndTime = origEndTime;

        this.startTime = TimeUtils.shiftOrderPmTime(origStartTime);
        this.endTime = TimeUtils.shiftOrderPmTime(origEndTime);

        this.startTime = TimeUtils.shiftOrderAmAuctionTime(this.startTime);
        this.endTime = TimeUtils.shiftOrderAmAuctionTime(this.endTime);


        this.strategy = strategy;
        this.ordType = new OrdType(priceType.equalsIgnoreCase("LIMIT")? OrdType.LIMIT: OrdType.MARKET);
        this.limitPx = limitPx;
        this.pov = pov;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setOrderId(SessionID sessionID) {
        if (StringUtils.isBlank(this.orderId)) {
            this.orderId = Constants.RunTimeId + "-" + sessionID.getSenderCompID() + "-" + UUID.randomUUID().toString();
        } else {
            this.orderId = Constants.RunTimeId + "-" + sessionID.getSenderCompID() + "-" + this.orderId;
        }
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

    public String getEndTime() {
        return endTime;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public String getStrategy() {
        return strategy;
    }

    public OrdType getOrdType() {
        return ordType;
    }

    public double getLimitPx() {
        return limitPx;
    }

    public double getPov() {
        return pov;
    }


    public NewOrderSingle toNewOrderRequest() {
        NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                new ClOrdID(UUID.randomUUID().toString()), new HandlInst('1'),
                new Symbol(symbol), side,
                new TransactTime(new Date(0)), ordType);

        newOrderSingle.set(new OrderQty(qty));
        if (ordType.getValue() == OrdType.LIMIT) {
            newOrderSingle.set(new Price(limitPx));
        }

        newOrderSingle.set(new Account(accountId));
        newOrderSingle.set(new SecondaryClOrdID(accountId));
        newOrderSingle.set(new SecurityExchange(exchange.getFixId()));

        newOrderSingle.setString(FixTags.EffectiveTime, startTime);
        newOrderSingle.setString(FixTags.ExpireTime, endTime);

        // TODO use client id to identify test group? Set this value before send to fix sessions.

        newOrderSingle.setString(FixTags.AlgoType, strategy);

        return newOrderSingle;
    }
}
