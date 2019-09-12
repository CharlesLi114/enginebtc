package com.csc108.enginebtc.admin;

/**
 * Created by LI JT on 2017/10/26.
 * Description:
 */
public class AdminConfig {

    private String ip;
    private int port;

    public AdminConfig(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
