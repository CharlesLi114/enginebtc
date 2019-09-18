package com.csc108.enginebtc.tdb.models;

import cn.com.wind.td.tdb.Tick;
import com.csc108.enginebtc.commons.AbstractTdbData;
import com.csc108.enginebtc.utils.Constants;
import com.csc108.enginebtc.utils.TimeUtils;
import com.csc108.enginebtc.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math.util.MathUtils;


import java.util.Map;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class MarketData extends AbstractTdbData {

    private String stockId;
    private String symbol;
    private String exchangeCode;
    private String selector;

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

    private double[] bidPx;
    private double[] askPx;

    private long[] bidVols;
    private long[] askVols;

    private int timeStamp;

    public MarketData(Tick tick) {
        this.isValid = isTimeValid(tick.getTime());


        // TODO status
        // this.status = null;

        this.stockId = tick.getWindCode();
        this.symbol = Utils.getSymbol(this.stockId);
        this.exchangeCode = Utils.getExchange(this.stockId);

        this.selector = "hq" + StringUtils.remove(this.stockId, ".").intern();



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
    public double[] getPrices(int[] values) {
        if (values == null) {
            values = new int[10];
        }
        double[] px = new double[values.length];
        for (int i = 0; i < values.length; i ++) {
            px[i] = values[i] / Constants.SCALE;
        }
        return px;

    }

    public long[] getVolumes(long[] values) {
        return values == null? new long[10]: values;
    }

    @Override
    public int getTime() {
        return this.timeStamp;
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
            builder.append("bidprice").append(i+1).append("=\"").append(String.format("%.4f", bidPx[i])).append("\" ");
            builder.append("bidvolume").append(i+1).append("=\"").append(this.bidVols[i]).append("\" ");
        }
        for (int i = 0; i < 5; i ++) {
            builder.append("askprice").append(i+1).append("=\"").append(String.format("%.4f", askPx[i])).append("\" ");
            builder.append("askvolume").append(i+1).append("=\"").append(this.askVols[i]).append("\" ");
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
}
