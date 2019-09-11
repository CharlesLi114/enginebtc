package com.csc108.enginebtc.cache;

import com.csc108.enginebtc.commons.Order;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.utils.TimeUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by LI JT on 2019/9/9.
 * Description:
 */
public class OrderCache {


    private Map<String, Order> cache;
    private String minStartTime;
    private int minTimestamp;
    private int date = 0;
    private Set<String> stockIds;


    public OrderCache() {
        this.cache = new HashMap<>();
        this.stockIds = new HashSet<>();
    }

    public void init() {

    }

    public void initFromFile() {

    }

    public void initFromDB() {

    }

    public void addOneOrder(Order order) {
        this.cache.put(order.getOrderId(), order);
    }

    public void computeMinTime() {
        LocalDateTime minTime = LocalDateTime.MAX;
        for (Order o : this.cache.values()) {
            LocalDateTime o_time = TimeUtils.convertOrderTime(o.getStartTime());
            if (o_time.isBefore(minTime)) {
                minTime = o_time;
            }
            if (date == 0) {
                date = o_time.getYear() * 10000 + o_time.getMonth().getValue() * 100 + o_time.getDayOfMonth();
            } else {
                int date1 = o_time.getYear() * 10000 + o_time.getMonth().getValue() * 100 + o_time.getDayOfMonth();
                if (date1 != date) {
                    throw new InitializationException("Two orders are of different dates, which is not supported.");
                }
            }
        }
        this.minStartTime = TimeUtils.toOrderTime(minTime);
        this.minTimestamp = minTime.getHour() * 10000000 * minTime.getMinute() * 100000 + minTime.getSecond() * 1000;
    }

    public int getMinTimestamp() {
        return this.minTimestamp;
    }

    public Map<String, Order> getOrders() {
        return cache;
    }

    public List<String> getStockIds() {
        if (this.stockIds.isEmpty()) {
            this.cache.forEach((key, value) -> stockIds.add(value.getStockId()));
        }

        return new ArrayList<>(this.stockIds);
    }

    public int getDate() {
        return this.date;
    }

    public void sendOrders() {

    }
}
