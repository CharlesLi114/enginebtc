package com.csc108.enginebtc;

import static com.csc108.enginebtc.admin.NettyServer.Netty;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class Application {






    public static void main(String[] args) throws InterruptedException {

        Netty.start();

        while (true) {
            Thread.sleep(10000);
        }


    }


}
