package com.csc108.enginebtc.tdb.models;

import cn.com.wind.td.tdb.Tick;
import cn.com.wind.td.tdb.TickAB;
import com.csc108.enginebtc.commons.AbstractTdbData;
import com.csc108.enginebtc.utils.Constants;
import com.csc108.enginebtc.utils.TimeUtils;
import com.csc108.enginebtc.utils.Utils;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.jms.JMSException;
import java.util.*;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class MarketData extends AbstractTdbData {

    private static final Logger logger = LoggerFactory.getLogger(MarketData.class);


    private String symbol;
    private String exchangeCode;
    private String selector;

    private char status;
    private long preClose;
    private long open;
    private long high;
    private long low;
    private long match;
    private long numTrades;
    private long volume;
    private long turnOver;
    private long lowLimited;
    private long highLimited;

    private List<Long> bidPx;       // List can be wrapped into ActiveMQMapMessage.setObject.
    private List<Long> askPx;

    private List<Long> bidVols;
    private List<Long> askVols;

    public MarketData(TickAB tick) {
        this.isValid = isTimeValid(tick.getTime());


        this.status = TimeUtils.getStatus(tick.getTime(), true);

        this.stockId = tick.getWindCode();
        this.symbol = Utils.getSymbol(this.stockId);
        this.exchangeCode = Utils.getExchange(this.stockId);

        this.selector = "hq" + StringUtils.remove(this.stockId, ".").intern();

        this.timestamp = TimeUtils.getTimeStamp(tick.getTime(), true);
        this.preClose = tick.getPreClose();
        this.open = tick.getOpen();
        this.high = tick.getHigh();
        this.low = tick.getLow();
        this.match = tick.getPrice();
        this.numTrades = tick.getItems();
        this.volume = tick.getVolume();
        this.turnOver = tick.getTurover();

        this.highLimited = (long) (MathUtils.round(this.preClose / Constants.SCALE * 1.1, 2) * Constants.SCALE);
        this.lowLimited = (long) (MathUtils.round(this.preClose / Constants.SCALE * 0.9, 2) * Constants.SCALE);


        this.bidPx = this.getPrices(tick.getBidPrice());
        this.askPx = this.getPrices(tick.getAskPrice());

        this.bidVols = this.getVolumes(tick.getBidVolume());
        this.askVols = this.getVolumes(tick.getAskVolume());

        this.timestamp = tick.getTime();
    }


    /**
     * Used to create values for tdf 3.0, in whose test case, if a new market data is created using TDF_MSG_DATA msg = new TDF_MSG_DATA(), its arrays are null, which is different from Tdf 2.0.
     * @param values, long array like volume and price
     * @return new long[10] if input is null
     */
    private List<Long> getPrices(int[] values) {
        if (values == null) {
            return new ArrayList<>(10);
        }
        List<Long> l = new ArrayList<>(values.length);
        for (int value : values) {
            l.add((long) value);
        }
        return l;
    }

    public List<Long> getVolumes(long[] values) {
        if (values == null) {
            return new ArrayList<>(10);
        }
        List<Long> l = new ArrayList<>(values.length);
        for (Long value : values) {
            l.add(value);
        }
        return l;
    }


    public String getSelector() {
        return this.selector;
    }

    public String getStockId() {
        return stockId;
    }

    @Override
    public String toXmlMsg() {
        String msg = "<Quot>" +
                "<head type=\"hq\" recordnum=\"1\" />" +
                "<body>" +
                "<record" +
                " stkcode=\"" + this.symbol + "\"" +
                " iopvvalue=\"0.0000\" " +
                " marketid=\"" + this.exchangeCode + "\"" +
                " stkname=\"" + this.stockId + "\"" +
                " isstop=\"" + "F" + "\"" +
                " preclose=\"" + String.format("%.4f", this.preClose) + "\"" +
                " lastprice=\"" + String.format("%.4f", this.match) + "\"" +
                " openprice=\"" + String.format("%.4f", this.open) + "\"" +
                " closeprice=\"" + String.format("%.4f", this.match) + "\"" +
                " highestprice=\"" + String.format("%.4f", this.high) + "\"" +
                " lowestprice=\"" + String.format("%.4f", this.low) + "\"" +
                " donevolume=\"" + this.volume + "\"" +
                " turnover=\"" + this.turnOver + "\" ";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i ++) {
            builder.append("bidprice").append(i+1).append("=\"").append(String.format("%.4d", bidPx.get(i))).append("\" ");
            builder.append("bidvolume").append(i+1).append("=\"").append(this.bidVols.get(i)).append("\" ");
        }
        for (int i = 0; i < 5; i ++) {
            builder.append("askprice").append(i+1).append("=\"").append(String.format("%.4d", askPx.get(i))).append("\" ");
            builder.append("askvolume").append(i+1).append("=\"").append(this.askVols.get(i)).append("\" ");
        }

        msg += builder.toString();
        msg += "settleprice=\"0.00\" openinterest=\"0.00\"";
        msg += " time=\"" + this.timestamp + "\"/>" + "</body>" + "</Quot>";
        return msg;
    }

    @Override
    public Map toMap() {
        return null;
    }

    @Override
    public ActiveMQMapMessage toMQMapMessage() {
        try {
            ActiveMQMapMessage msg = new ActiveMQMapMessage();
            msg.setString("Type", "MarketData");
            msg.setString("StockId", this.stockId);
            msg.setString("Symbol", this.symbol);
            msg.setString("Exchange", this.exchangeCode);

            msg.setChar("Status", this.status);
            msg.setLong("Preclose", this.preClose);
            msg.setLong("Open", this.open);
            msg.setLong("High", this.high);
            msg.setLong("Low", this.low);
            msg.setLong("Match", this.match);
            msg.setLong("NumTrades", this.numTrades);
            msg.setLong("Volume", this.volume);
            msg.setLong("Turnover", this.turnOver);

            msg.setLong("HighLimited", this.highLimited);
            msg.setLong("LowLimited", this.lowLimited);



            msg.setObject("BidPrices", this.bidPx);
            msg.setObject("AskPrices", this.askPx);
            msg.setObject("BidVolumes", this.bidVols);
            msg.setObject("AskVolumes", this.askVols);
            msg.setInt("Timestamp", this.timestamp);

            return msg;
        } catch (JMSException e) {
            logger.error("Error during formatting market data MapMessage", e);
            return null;
        }
    }
}
