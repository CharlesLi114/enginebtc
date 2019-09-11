package com.csc108.enginebtc.cache;

import quickfix.SessionID;

/**
 * Created by zhangbaoshan on 2017/1/19.
 */
public class SessionIdWrapper {
    //peg;normal;
    private final boolean peggingSession;
    private final boolean acceptorSession;
    private final SessionID sessionID;

    private boolean loggedOn=true;

    public SessionIdWrapper(SessionID _sessionID, boolean isAcceptor) {
        if (_sessionID.getTargetCompID().contains("PEG")) {
            peggingSession = true;
        } else {
            peggingSession = false;
        }
        acceptorSession = isAcceptor;
        sessionID = _sessionID;
    }

    public boolean isPeggingSession() {
        return peggingSession;
    }

    public boolean isAcceptorSession() {
        return acceptorSession;
    }

    public SessionID getSessionID() {
        return sessionID;
    }

    public boolean isLoggedOn() {
        return loggedOn;
    }

    public void setLoggedOn(boolean _loggedOn) {
        loggedOn = _loggedOn;
    }
}