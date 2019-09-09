package com.csc108.enginebtc.sessions;

import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.commons.Exchange;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LI JT on 2019/9/6.
 * Description:
 */
public class SessionController extends AbstractLifeCircleBean {


    public static SessionController Sessions = new SessionController();


    private Map<Exchange, SessionGroup> sessions;


    private SessionController() {
        this.sessions = new HashMap<>();
        this.config();
    }


    @Override
    public void config() {
        for (Exchange exchange : Exchange.values()) {
            SessionGroup group = new SessionGroup(exchange);
            this.sessions.put(exchange, group);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public SessionGroup getSessionGroup(Exchange exchange) {
        return this.sessions.get(exchange);
    }


}
