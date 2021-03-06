package com.csc108.enginebtc.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by LI JT on 2019/9/6.
 * Description:
 */
public class Constants {

    public static final Double SCALE = 10000.0;

    public static final String RunTimeId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));

    public static final String CalcReadyMsg = "CALCREADY";


}
