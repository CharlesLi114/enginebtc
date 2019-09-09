package com.csc108.enginebtc.controller;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class Controller {


    public static Controller Controller = new Controller();


    private volatile boolean isSystemReady = false;
    private volatile boolean isCalcReady = false;

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









    public boolean isSystemReady() {
        return this.isSystemReady;
    }

    public boolean isCalcReady() {
        return this.isCalcReady;
    }


}
