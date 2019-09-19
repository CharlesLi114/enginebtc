package com.csc108.enginebtc.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final int AM_AUCTION_SHIFT_TIME_SECONDS = 4 * 60;

    private static final SimpleDateFormat CSC_FORMAT = new SimpleDateFormat("HHmmssSSS");
    private static final DateTimeFormatter Order_DateTime_Format = DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss.SSS");    //2017-07-17 15:00:00.000

    public static int getTimeStamp(int timestamp, boolean isAShare) {
        if (isAShare) {
            if (timestamp > 130000000) {
                return addSeconds(timestamp, -MOON_SECONDS);
            } else if (timestamp >= 91500000 && timestamp < 93000000) {
                return addSeconds(timestamp, AM_AUCTION_SHIFT_TIME_SECONDS);
            } else {
                return timestamp;
            }
        } else {
            // TODO
            return timestamp;
        }
    }


    private static Calendar getCalender(int timeStamp) {
        int hour = timeStamp / HOUR_DIVIDER;
        int other = timeStamp - hour * HOUR_DIVIDER;
        int minute = other / MINUTE_DIVIDER;
        other = other - minute * MINUTE_DIVIDER;
        int second = other / SECOND_DIVIDER;
        int millis = other - second * SECOND_DIVIDER;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millis);

        return calendar;
    }

    /**
     *
     * @param time timeStamp
     * @param amount time in seconds
     * @return
     */
    public static int addSeconds(int time, int amount) {
        Calendar calendar = getCalender(time);

        calendar.add(Calendar.SECOND, amount);

        return Integer.parseInt(CSC_FORMAT.format(new Date(calendar.getTimeInMillis())));
    }

    public static int addMiliis(int time, int amount) {
        Calendar calendar = getCalender(time);

        calendar.add(Calendar.MILLISECOND, amount);

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


    /**
     * Shift pm time 1.5 hours early, time 2017-07-17 15:00:00.000 is converted to 2017-07-17 13:30:00.000
     * @param o_time    2017-07-17 15:00:00.000
     * @return
     */
    public static String shiftOrderPmTime(String o_time) {
        LocalDateTime time = LocalDateTime.parse(o_time, Order_DateTime_Format);
        if (time.getHour() >= 13) {
            time = time.plusMinutes(-90);
            return time.format(Order_DateTime_Format);
        } else  {
            return o_time;
        }
    }

    public static String shiftOrderAmAuctionTime(String o_time) {
        LocalDateTime time = LocalDateTime.parse(o_time, Order_DateTime_Format);
        if (time.getHour() == 9 && time.getMinute() < 30) {
            time = time.plusMinutes(4);
            return time.format(Order_DateTime_Format);
        } else {
            return o_time;
        }
    }

    public static LocalDateTime orderTimeConvert(String o_time) {
        return LocalDateTime.parse(o_time, Order_DateTime_Format);
    }

    /**
     * Parse order time.
     * @param o_time
     * @return
     */
    public static LocalDateTime convertOrderTime(String o_time) {
        return LocalDateTime.parse(o_time, Order_DateTime_Format);
    }

    public static String toOrderTime(LocalDateTime dt) {
        return dt.format(Order_DateTime_Format);
    }

}
