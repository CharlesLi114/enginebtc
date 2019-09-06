package com.csc108.enginebtc.tdb.models;

import cn.com.wind.td.tdb.Transaction;
import com.csc108.enginebtc.commons.AbstractTdbData;
import com.csc108.enginebtc.utils.Constants;
import com.csc108.enginebtc.utils.TimeUtils;

import java.util.Map;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class TransactionData extends AbstractTdbData {

    private double price;
    private double volume;
    private double turnover;


    public TransactionData(Transaction transaction) {
        this.price = transaction.getTradePrice() / Constants.SCALE;
        this.volume = transaction.getTradeVolume();
        this.turnover = this.price * this.volume;
        this.timestamp = TimeUtils.getTimeStamp(transaction.getTime(), true);
    }


    @Override
    public int getTime() {
        return 0;
    }

    @Override
    public String toXmlMsg() {
        return null;
    }

    @Override
    public Map toMap() {
        return null;
    }
}
