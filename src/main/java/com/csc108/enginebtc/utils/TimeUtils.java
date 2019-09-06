package com.csc108.enginebtc.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by LI JT on 2019/9/6.
 * Description:
 */
public class TimeUtils {

    // Divider used to get minute from time stamp.
    private static final int HOUR_DIVIDER = 10000000;
    private static final int MINUTE_DIVIDER = 100000;
    private static final int SECOND_DIVIDER = 1000;

    private static final int MOON_SECONDS = 90 * 60;

    private static final SimpleDateFormat CSC_FORMAT = new SimpleDateFormat("HHmmssSSS");

    public static int getTimeStamp(int timestamp, boolean isAShare) {
        if (isAShare) {
            if (timestamp > 130000000) {
                return addSeconds(timestamp, -MOON_SECONDS);
            } else {
                return timestamp;
            }
        } else {
            // TODO
            return timestamp;
        }
    }


    /**
     *
     * @param time timeStamp
     * @param amount time in seconds
     * @return
     */
    public static int addSeconds(int time, int amount) {
        int hour = time / HOUR_DIVIDER;
        int other = time - hour * HOUR_DIVIDER;
        int minute = other / MINUTE_DIVIDER;
        other = other - minute * MINUTE_DIVIDER;
        int second = other / SECOND_DIVIDER;
        int millis = other - second * SECOND_DIVIDER;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millis);

        calendar.add(Calendar.SECOND, amount);

        return Integer.parseInt(CSC_FORMAT.format(new Date(calendar.getTimeInMillis())));
    }


    /**
     *
     * @param time      930
     * @param amount    1
     * @return
     */
    public static int addMinute(int time, int amount) {
        int minute = time % 100;
        int hour = (time - minute) / 100;
        minute += amount;
        hour = hour + minute / 60;
        minute = minute % 60;
        return hour * 100 + minute;
    }

}
