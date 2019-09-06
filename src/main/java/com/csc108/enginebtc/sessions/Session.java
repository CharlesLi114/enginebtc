package com.csc108.enginebtc.sessions;

import com.csc108.enginebtc.tdb.models.AuctType;
import com.csc108.enginebtc.tdb.models.ExchangeStatus;

/**
 * Created by LI JT on 2019/9/6.
 * Description:
 */
public class Session {

    private int startMinute;    // Inclusive
    private int endMinute;      // Inclusive
    private ExchangeStatus status;
    private AuctType auctionType;

    public Session(int startMinute, int endMinute, ExchangeStatus status, AuctType auctionType) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.status = status;
        this.auctionType = auctionType;
    }

    public Session() {
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public ExchangeStatus getStatus() {
        return status;
    }

    public AuctType getAuctionType() {
        return auctionType;
    }


    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public void setStatus(ExchangeStatus status) {
        this.status = status;
    }

    public void setAuctionType(AuctType auctionType) {
        this.auctionType = auctionType;
    }

    public boolean isSession(int timeInMin) {
        return timeInMin >= startMinute && timeInMin <= endMinute;
    }

}
