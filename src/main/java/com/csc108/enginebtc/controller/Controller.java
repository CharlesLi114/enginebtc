package com.csc108.enginebtc.controller;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class Controller {


    private static int speed = 1;
    private static int warmupSecs = 60;

    static {
    // Static initialization

    }




    public static int getSpeed() {
        return speed;
    }

    public static int getWarmupSecs() {
        return warmupSecs;
    }


}
