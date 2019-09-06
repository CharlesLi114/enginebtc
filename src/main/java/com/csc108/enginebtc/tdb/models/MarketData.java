package com.csc108.enginebtc.tdb.models;

import cn.com.wind.td.tdb.Tick;
import com.csc108.enginebtc.commons.AbstractTdbData;
import com.csc108.enginebtc.utils.Constants;
import com.csc108.enginebtc.utils.TimeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.util.MathUtils;


import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class MarketData extends AbstractTdbData {


    private char status;
    private double preClose;
    private double open;
    private double high;
    private double low;
    private double match;
    private long numTrades;
    private double volume;
    private double turnOver;
    private double lowLimited;
    private double highLimited;
    private double indauc;
    private double indaucs;

    private double bestBid;
    private double bestAsk;

    private int arrivalTime;
    private boolean isValid = true;

    public MarketData(Tick tick) {
        // TODO status
        // this.status = null;

        this.timestamp = TimeUtils.getTimeStamp(tick.getTime(), true);
        this.preClose = tick.getPreClose() / Constants.SCALE;
        this.open = tick.getOpen() / Constants.SCALE;
        this.high = tick.getHigh() / Constants.SCALE;
        this.low = tick.getLow() / Constants.SCALE;
        this.match = tick.getPrice() / Constants.SCALE;
        this.numTrades = tick.getMatchItems();
        this.volume = tick.getVolume();
        this.turnOver = tick.getTurover();

        this.highLimited = MathUtils.round(this.match * 1.1, 2);
        this.lowLimited = MathUtils.round(this.match * 0.9, 2);


        if (ArrayUtils.isNotEmpty(tick.getAskPrice())
                && ArrayUtils.isNotEmpty(tick.getAskVolume())) {
            this.indauc = this.getArrayValues(tick.getAskPrice())[0] / Constants.SCALE;
            this.indaucs = this.getArrayValues(tick.getAskVolume())[0];
        }
        this.bestAsk = this.getArrayValues(tick.getAskPrice())[0] / Constants.SCALE;
        this.bestBid = this.getArrayValues(tick.getBidPrice())[0] / Constants.SCALE;
    }


    /**
     * Used to create values for tdf 3.0, in whose test case, if a new market data is created using TDF_MSG_DATA msg = new TDF_MSG_DATA(), its arrays are null, which is different from Tdf 2.0.
     * @param values, long array like volume and price
     * @return new long[10] if input is null
     */
    public int[] getArrayValues(int[] values) {
        return values == null? new int[10]: values;
    }

    public long[] getArrayValues(long[] values) {
        return values == null? new long[10]: values;
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
