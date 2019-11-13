package com.csc108.enginebtc;

import com.csc108.enginebtc.admin.NettyListener;
import com.csc108.enginebtc.utils.SyncUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LI JT on 2019/11/13.
 * Description:
 */
public class TestSendToCalc {

    public static void main(String[] args) {



        List<String> calcs = new ArrayList<>();
        calcs.add("10.101.195.9:9202");

        List<String> stocks = new ArrayList<>();
        stocks.add("600000.SH;000001.SZ");
        SyncUtils.syncStocksWithCalc(calcs, stocks, 20191112, 130500000);

    }





}
