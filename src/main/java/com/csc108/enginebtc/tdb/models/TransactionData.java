package com.csc108.enginebtc.tdb.models;

import cn.com.wind.td.tdb.Transaction;
import com.csc108.enginebtc.amq.ActiveMqController;
import com.csc108.enginebtc.commons.AbstractTdbData;
import com.csc108.enginebtc.utils.Constants;
import com.csc108.enginebtc.utils.TimeUtils;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class TransactionData extends AbstractTdbData {

    private static final Logger logger = LoggerFactory.getLogger(TransactionData.class);

    private int intPrice;
    private double price;
    private int volume;
    private double turnover;
    private char funcCode;

    public TransactionData(Transaction transaction) {
        this.stockId = transaction.getWindCode();
        this.intPrice = transaction.getTradePrice();
        this.price = transaction.getTradePrice() / Constants.SCALE;
        this.volume = transaction.getTradeVolume();
        this.turnover = this.price * this.volume;
        this.funcCode = transaction.getFunctionCode();
        this.timestamp = TimeUtils.getTimeStamp(transaction.getTime(), true);

        this.isValid = isTimeValid(transaction.getTime());
    }



    @Override
    public String toXmlMsg() {
        return null;
    }

    @Override
    public Map toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("StockId", this.stockId);
        map.put("IntPx", this.intPrice);
        map.put("Price", this.price);
        map.put("Volume", this.volume);
        map.put("Turnover", this.turnover);
        map.put("FunctionCode", this.funcCode);
        return map;
    }

    public ActiveMQMapMessage toMQMapMessage() {
        try {
            ActiveMQMapMessage msg = new ActiveMQMapMessage();
            msg.setString("Type", "TransactionData");
            msg.setString("StockId", this.stockId);
            msg.setInt("IntPx", this.intPrice);
            msg.setDouble("Price", this.price);
            msg.setInt("Volume", this.volume);
            msg.setDouble("Turnover", this.turnover);
            msg.setChar("FunctionCode", this.funcCode);
            msg.setInt("Timestamp", this.timestamp);

            return msg;
        } catch (JMSException e) {
            logger.error("Error during formatting transaction MapMessage", e);
            return null;
        }
    }
}
