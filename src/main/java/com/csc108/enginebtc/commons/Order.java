package com.csc108.enginebtc.commons;

import com.csc108.enginebtc.exception.InvalidOrderException;
import com.csc108.enginebtc.fix.FixTags;
import com.csc108.enginebtc.utils.Constants;
import com.csc108.enginebtc.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * Created by LI JT on 2019/9/9.
 * Description: ClientHandler order
 *
 *
 * 2017-07-17 13:00:00.000
 */
public class Order {


    private String accountId;

    private String orderId;
    private String stockId;
    private String symbol;
    private String origStartTime;   // 2017-07-17 15:00:00.000
    private String origEndTime;

    private String startTime;       // Converted time string that is used in fix 6062, 6062=20191114-01:30:03
    private String endTime;         // Converted time string that is used in fix 6062, 6062=20191114-01:30:03

    private Exchange exchange;
    private Side side;
    private int qty;


    private String strategy;
    private OrdType ordType;
    private double limitPx;
    private int pov;                // Pov is a integer from 0 to 100



    /**
     *
     * @param orderId nullable
     * @param stockId
     * @param origStartTime
     * @param origEndTime
     * @param strategy
     * @param priceType
     * @param limitPx
     * @param pov
     */
    public Order(String orderId, String accountId, String stockId, String origStartTime, String origEndTime, String strategy, String priceType, double limitPx, double pov, String side, int qty, String exDest) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.stockId = stockId;
        this.symbol = StringUtils.substringBefore(stockId, ".");
        this.origStartTime = origStartTime;
        this.origEndTime = origEndTime;
        this.side = side.toUpperCase().equalsIgnoreCase("BUY")? new Side('1'): new Side('2');
        this.exchange = Exchange.parse(exDest);

        // Have to convert date to today.
        this.startTime = TimeUtils.shiftOrderPmTime(origStartTime);
        this.endTime = TimeUtils.shiftOrderPmTime(origEndTime);

        this.startTime = TimeUtils.shiftOrderAmAuctionTime(this.startTime);
        this.endTime = TimeUtils.shiftOrderAmAuctionTime(this.endTime);


        this.strategy = strategy;
        this.ordType = new OrdType(priceType.equalsIgnoreCase("LIMIT")? OrdType.LIMIT: OrdType.MARKET);
        this.limitPx = limitPx;
        if (pov < 0) {
            this.pov = 0;
        } else {
            this.pov = (int) (pov * 100);
        }

        this.qty = qty;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * Create unique order id with identification.
     * @param sessionID
     */
    public String getSysOrderId(SessionID sessionID) {
        String sysOrderId;
        if (StringUtils.isBlank(this.orderId)) {
            sysOrderId = Constants.RunTimeId + "-" + sessionID.getSenderCompID() + "-" + UUID.randomUUID().toString();
        } else {
            sysOrderId = Constants.RunTimeId + "-" + sessionID.getSenderCompID() + "-" + this.orderId;
        }
        return sysOrderId;
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

    public int getPov() {
        return pov;
    }


    public NewOrderSingle toNewOrderRequest(SessionID sessionID) {
        String sysOrderId = this.getSysOrderId(sessionID);

        NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                new ClOrdID(sysOrderId),
                new HandlInst('1'),
                new Symbol(symbol), side,
                new TransactTime(new Date(0)), ordType);

        newOrderSingle.set(new OrderQty(qty));
        if (ordType.getValue() == OrdType.LIMIT) {
            newOrderSingle.set(new Price(limitPx));
        }
        newOrderSingle.set(new ExDestination(this.exchange.getFixId()));

        newOrderSingle.set(new Account(accountId));
        newOrderSingle.set(new SecondaryClOrdID(accountId));
        newOrderSingle.set(new SecurityExchange(exchange.getFixId()));

        newOrderSingle.setString(FixTags.EffectiveTime, TimeUtils.toFixMsgTime(startTime));
        newOrderSingle.setString(FixTags.ExpireTime, TimeUtils.toFixMsgTime(endTime));

        newOrderSingle.setString(FixTags.AlgoType, strategy);
        newOrderSingle.setInt(FixTags.ParticipationRate, this.pov);
        newOrderSingle.setString(FixTags.SecurityType, "CS");

        newOrderSingle.set(new ClientID(sessionID.toString()));
        newOrderSingle.set(new Currency("CNY"));
        return newOrderSingle;
    }

    public OrderCancelRequest toCancelRequest(SessionID sessionID) {
        String sysOrderId = this.getSysOrderId(sessionID);
        OrderCancelRequest cancelRequest =
                new OrderCancelRequest(
                        new OrigClOrdID(sysOrderId),
                        new ClOrdID(UUID.randomUUID().toString()),
                        new Symbol(getSymbol()),
                        this.side,
                        new TransactTime());
        cancelRequest.set(new SecondaryClOrdID(accountId));
        cancelRequest.setString(FixTags.SecurityType, "CS");
        cancelRequest.setString(FixTags.AccountID, this.accountId);

        return cancelRequest;

    }

    /**
     * Check if this order is valid, if so will send for backtest.
     * A valid order is that considered appropriate for this backtest system.
     * @return
     */
    public void validate() throws InvalidOrderException {
        LocalDateTime t1 = TimeUtils.orderTimeConvert(this.startTime);
        LocalDateTime t2 = TimeUtils.orderTimeConvert(this.endTime);
        if ((t1.getHour() <= 9 && t1.getMinute() < 30) && (t2.getHour() <= 9 && t2.getMinute() < 30)) {
            throw new InvalidOrderException("Do not support am auction only orders.");
        }
        if ((t1.getHour() == 14 && t1.getMinute() >= 57) && (t2.getHour() == 14 && t2.getMinute() >= 57)) {
            throw new InvalidOrderException("Do not support pm auction only orders.");
        }

        if (t2.isBefore(t1)) {
            throw new InvalidOrderException("End time is before start time.");
        }



    }

    @Override
    public String toString() {
        return (new ReflectionToStringBuilder(this)).toString();
    }



}
