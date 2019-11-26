package com.csc108.enginebtc.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private static int ACTION_DAY = Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    private static final SimpleDateFormat CSC_FORMAT = new SimpleDateFormat("HHmmssSSS");
    private static final DateTimeFormatter Order_DateTime_Format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");    //2017-07-17 15:00:00.000
    private static final DateTimeFormatter Fix_Time_Format = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss");   //6063=20191114-01:30:03
    private static final DateTimeFormatter DB_DateTime_Format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    public static int getActionDay() {
        return ACTION_DAY;
    }

    /**
     * May convert isAShare to an Enum. TODO
     * @param timestamp
     * @param isAShare
     * @return
     */
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
            return timestamp;
        }
    }

    /**
     * Get current time stamp in CSC format.
     * @return the current time stamp in CSC format.
     * */
    public static int getTimeStamp() {
        Calendar calendar = Calendar.getInstance();

        return Integer.parseInt(CSC_FORMAT.format(new Date(calendar.getTimeInMillis())));
    }

    /**
     * Infer Tdf status using its timestamp, currently for Ashare only.
     * * May convert isAShare to an Enum. TODO
     * @return
     */
    public static char getStatus(int timestamp, boolean isAshare) {
        if (isAshare) {
            if (timestamp < 91500000) {
                return 'F';
            }
            if (timestamp < 93000000) {
                return 'I';
            }
            if (timestamp < 145700000) {
                return 'O';
            }
            if (timestamp < 150000000) {
                return 'J';
            }
            return 'C';
        } else {
            return 'O';
        }
    }


    public static Calendar getCalender(int timeStamp) {
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
     * Convert timestamp to localTime
     */
    public static LocalTime tsToLt(int timeStamp) {
        int hour = timeStamp / HOUR_DIVIDER;
        int other = timeStamp - hour * HOUR_DIVIDER;
        int minute = other / MINUTE_DIVIDER;
        other = other - minute * MINUTE_DIVIDER;
        int second = other / SECOND_DIVIDER;

        return LocalTime.of(hour, minute, second);
    }


    /**
     * Shift pm time 1.5 hours early, time 2017-07-17 15:00:00.000 is converted to 2017-07-17 13:30:00.000
     * This method also converts trading day to today for engine normalization requires same day input.
     * @param o_time    2017-07-17 15:00:00.000
     * @return
     */
    public static String shiftOrderPmTime(String o_time) {
        LocalDateTime time = LocalDateTime.parse(o_time, Order_DateTime_Format);

        time = LocalDateTime.now().withHour(time.getHour()).withMinute(time.getMinute()).withSecond(time.getSecond());
        time = LocalDateTime.now().withHour(time.getHour()).withMinute(time.getMinute()).withSecond(time.getSecond());
        if (time.getHour() >= 13) {
            time = time.plusMinutes(-90);
            return time.format(Order_DateTime_Format);
        } else  {
            return time.format(Order_DateTime_Format);
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

    public static int formatTradeDate(String tradingDay) {
        LocalDateTime date = LocalDateTime.parse(tradingDay, DB_DateTime_Format);
        return date.getYear() * 10000 + date.getMonth().getValue() * 100 + date.getDayOfMonth();
    }



    /**
     * 1. Convert order start/end time to fix msg type, which is of 20191114-01:30:03
     * 2. Minus time by 8 hours.
     * @param o_time of pattern Order_DateTime_Format, yyyy-MM-dd HH:mm:ss.SSS
     * @return
     */
    public static String toFixMsgTime(String o_time) {
        LocalDateTime time = LocalDateTime.parse(o_time, Order_DateTime_Format);
        time = time.minusHours(8);
        return time.format(Fix_Time_Format);
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
