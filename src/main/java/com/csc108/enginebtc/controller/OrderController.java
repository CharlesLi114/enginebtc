package com.csc108.enginebtc.controller;

import com.csc108.enginebtc.cache.FixSessionCache;
import com.csc108.enginebtc.cache.OrderCache;
import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.commons.Order;
import com.csc108.enginebtc.fix.FixMSgSender;
import quickfix.SessionID;
import quickfix.field.ClientID;
import quickfix.fix42.NewOrderSingle;

import java.util.List;
import java.util.Map;

/**
 * Created by LI JT on 2019/9/10.
 * Description:
 */
public class OrderController extends AbstractLifeCircleBean {


    public static OrderController OrderController = new OrderController();

    private OrderCache cache;



    private OrderController() {
        this.cache = new OrderCache();
        this.cache.init();
    }



    @Override
    public void config() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public int getMinTimeStamp() {
        return this.cache.getMinTimestamp();
    }

    public void sendOrder() {
        Map<String, Order> orders = this.cache.getOrders();
        for (Order o : orders.values()) {
            NewOrderSingle newOrderSingle = o.toNewOrderRequest();
            List<SessionID> sessions = FixSessionCache.getInstance().getSessions();
            for (SessionID session : sessions) {
                newOrderSingle.set(new ClientID(session.toString()));
                FixMSgSender.sendNow(newOrderSingle, session);

            }
        }
    }
}
