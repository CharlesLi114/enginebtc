package com.csc108.enginebtc.commons;

import java.util.Map;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public abstract class AbstractTdbData <T> {


    protected int timestamp;

    protected boolean isValid;

    public boolean passedTime(int time) {
        return time <= timestamp;
    }

    public abstract int getTime();

    public abstract String toXmlMsg();

    public abstract Map toMap();

    public boolean isValid() {
        return this.isValid;
    }

    /**
     * There kinds of data are invalid:
     * 1. original timestamp between 113000000 and 130000000, filter out market noon break time;
     * @param origTimestamp
     * @return
     */
    public boolean isTimeValid(int origTimestamp) {
        if (origTimestamp >= 113000000 && origTimestamp < 130000000) {
            return false;
        }

        return true;
    }



}
