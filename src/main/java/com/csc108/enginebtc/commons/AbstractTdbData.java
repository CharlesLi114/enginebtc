package com.csc108.enginebtc.commons;

import java.util.Map;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public abstract class AbstractTdbData <T> {


    protected int timestamp;

    public boolean passedTime(int time) {
        return time <= timestamp;
    }

    public abstract int getTime();

    public abstract String toXmlMsg();

    public abstract Map toMap();



}
