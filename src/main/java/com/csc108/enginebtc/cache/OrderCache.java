package com.csc108.enginebtc.cache;

import com.csc108.enginebtc.commons.Order;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.exception.InvalidOrderException;
import com.csc108.enginebtc.fix.FixMSgSender;
import com.csc108.enginebtc.utils.ConfigUtil;
import com.csc108.enginebtc.utils.FileUtils;
import com.csc108.enginebtc.utils.TimeUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.SessionID;
import quickfix.field.ClientID;
import quickfix.fix42.NewOrderSingle;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by LI JT on 2019/9/9.
 * Description:
 */
public class OrderCache {

    private static final Logger logger = LoggerFactory.getLogger(OrderCache.class);

    private static final String OrderSrc_Config_File = "orderconfig.properties";

    private static final String OrderSrc_Type_Property_Name = "OrderSrc";
    private static final String OrderFile_Type_Porperty_Name = "File.Dir";


    public static final OrderCache OrderCache = new OrderCache();

    private Map<String, Order> cache;
    private String minStartTime;
    private int minTimestamp;
    private int date = 0;
    private Set<String> stockIds;


    private OrderCache() {
        this.cache = new HashMap<>();
        this.stockIds = new HashSet<>();
    }

    public void start() {
        String configFile = ConfigUtil.getConfigPath(OrderSrc_Config_File);
        try {
            Configuration config = new PropertiesConfiguration(configFile);
            OrderSrcType srcType = OrderSrcType.valueOf(config.getString(OrderSrc_Type_Property_Name).toUpperCase());
            if (srcType.equals(OrderSrcType.FILE)) {
                this.initFromFile(config);
            } else {
                this.initFromDB(config);
            }


        } catch (ConfigurationException | FileNotFoundException e) {
            e.printStackTrace();
        }


        this.computeMinTime();
    }

    /**
     * File contains data which is in the following order.
     SELECT accountId, orderId, exDestination, symbol, tradingDay, side,
     type, price, algo, effectiveTime, expireTime,
     orderQty, participationRate
     FROM dbo.ClientOrderView WHERE tradingDay = '20191113' AND symbol NOT LIKE '204%' ORDER BY cumQty DESC
     * @param config
     * @throws FileNotFoundException
     */
    public void initFromFile(Configuration config) throws FileNotFoundException {
        String file = config.getString(OrderFile_Type_Porperty_Name);
        List<List<String>> contents = FileUtils.readCsv(file);
        int index;
        for (List<String> rowContent : contents) {
            index = 0;
            String accountId = rowContent.get(index++);
            String orderId = rowContent.get(index++);
            String exchange = rowContent.get(index++);
            String stockId = rowContent.get(index++);
            String tradingDay = rowContent.get(index++);
            String side = rowContent.get(index++);
            String type = rowContent.get(index++);
            double price = Double.valueOf(rowContent.get(index++));
            String algo = rowContent.get(index++);
            String startTime = rowContent.get(index++);
            String endTime = rowContent.get(index++);
            int qty = Integer.valueOf(rowContent.get(index++));
            double pov = Double.valueOf(rowContent.get(index));

            Order order = new Order(orderId, accountId, stockId, startTime, endTime, algo, type, price, pov, side, qty, exchange);
            this.cache.put(orderId, order);
        }
    }

    public void initFromDB(Configuration config) {
        // TODO
    }

    private void computeMinTime() {
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
        this.minTimestamp = minTime.getHour() * 10000000 + minTime.getMinute() * 100000 + minTime.getSecond() * 1000;
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



    public void publishOrders() {
        Map<String, Order> orders = this.cache;
        for (Order o : orders.values()) {
            try {
                o.validate();
            } catch (InvalidOrderException e) {
                logger.warn("Invalid order: " +e.getMessage());
                logger.warn(o.toString());
            }

            NewOrderSingle newOrderSingle = o.toNewOrderRequest();
            List<SessionID> sessions = FixSessionCache.getInstance().getSessions();
            for (SessionID session : sessions) {
                o.resetUniqueOrderId(session);
                newOrderSingle.set(new ClientID(session.toString()));
                FixMSgSender.sendNow(newOrderSingle, session);
            }
        }
    }


    private enum OrderSrcType {
        FILE,

        DB,
    }
}
