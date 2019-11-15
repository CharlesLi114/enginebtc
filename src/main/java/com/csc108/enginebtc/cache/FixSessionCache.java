package com.csc108.enginebtc.cache;

import com.csc108.enginebtc.utils.ConfigUtil;
import com.csc108.enginebtc.utils.FileUtils;
import com.csc108.enginebtc.utils.FormattedTable;
import quickfix.SessionID;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by NIUXX on 2017/1/7.
 */
public class FixSessionCache {


    private final ConcurrentHashMap<String, SessionIdWrapper> sessionMap = new ConcurrentHashMap<>();
    private ReentrantReadWriteLock lock;
    private Lock readLock;
    private Lock writeLock;
    private volatile int expectedSessionNb = 0;

    private ArrayList<SessionID> acceptorSessions;
    private ArrayList<SessionID> peggingSessions;
    private ArrayList<SessionID> normalSessions;

    private AtomicInteger normalCounter = new AtomicInteger(0);
    private AtomicInteger peggingCounter = new AtomicInteger(0);

    public ConcurrentHashMap<String, SessionIdWrapper> getSessionMap() {
        return sessionMap;
    }

    private final static FixSessionCache instance = new FixSessionCache();

    public static FixSessionCache getInstance() {
        return instance;
    }

    public void clear() {
        getSessionMap().clear();
        peggingSessions = new ArrayList<>();
        normalSessions = new ArrayList<>();
        acceptorSessions = new ArrayList<>();
        normalCounter = new AtomicInteger(0);
        peggingCounter = new AtomicInteger(0);
    }

    private FixSessionCache() {
        acceptorSessions = new ArrayList<>();
        peggingSessions = new ArrayList<>();
        normalSessions = new ArrayList<>();
        boolean fairProcess = true;
        this.lock = new ReentrantReadWriteLock(fairProcess);
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    public void addSession(SessionID sessionID, boolean isAcceptor) {
        this.writeLock.lock();
        try {
            SessionIdWrapper sessionIdWrapper = new SessionIdWrapper(sessionID, isAcceptor);
//            Alert.clearAlert(String.format(Alert.SESSION_CONNECTION_ERROR, sessionIdWrapper.getSessionID().toString()));
            sessionMap.putIfAbsent(sessionID.toString(), sessionIdWrapper);
            sessionMap.get(sessionID.toString()).setLoggedOn(true);
            if (isAcceptor) {
                addSessionToList(acceptorSessions, sessionID);
            } else if (sessionIdWrapper.isPeggingSession()) {
                addSessionToList(peggingSessions, sessionID);
            } else {
                addSessionToList(normalSessions, sessionID);
            }
            String alterKey = String.format("Session(%s)_IS_MISSING_OR_NOT_LOGON", sessionID);
//            Alert.clearAlert(alterKey);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void setExpectedSessionNb(int nb) {
        this.expectedSessionNb = nb;
    }

    public boolean isFixSessionReady() {
        return this.expectedSessionNb != 0 && this.expectedSessionNb == normalSessions.size();
    }

    public List<SessionID> getSessions() {
        return this.normalSessions;
    }

    public void removeSession(SessionID sessionID) {
        this.writeLock.lock();
        try {
            SessionIdWrapper idWrapper = sessionMap.get(sessionID.toString());
            if (idWrapper != null) {
                idWrapper.setLoggedOn(false);
//                Alert.fireAlert(Alert.Severity.Fatal, String.format(Alert.SESSION_CONNECTION_ERROR, idWrapper.getSessionID().toString()), "Session logged out unexpectedly!", null);
                if (idWrapper.isAcceptorSession()) {
                    removeSessionFromList(acceptorSessions, idWrapper.getSessionID());
                } else if (idWrapper.isPeggingSession()) {
                    removeSessionFromList(peggingSessions, idWrapper.getSessionID());
                } else {
                    removeSessionFromList(normalSessions, idWrapper.getSessionID());
                }
            }
        } finally {
            this.writeLock.unlock();
        }
    }



    @Override
    public String toString() {
        FormattedTable table = new FormattedTable();
        List<Object> row = new ArrayList<Object>(3);
        row.add("Session");
        row.add("Type");
        row.add("IsLoggedOn");
        table.AddRow(row);

        Enumeration<String> keyIterator = getInstance().sessionMap.keys();
        while (keyIterator.hasMoreElements()) {
            row = new ArrayList<>(3);
            String key = keyIterator.nextElement();
            SessionIdWrapper idWrapper = getInstance().sessionMap.get(key);
            row.add(idWrapper.getSessionID().toString());
            row.add(idWrapper.isAcceptorSession() ? "Acceptor" : idWrapper.isPeggingSession() ? "Pegging" : "Normal");
            row.add(idWrapper.isLoggedOn() ? "True" : "False");
            table.AddRow(row);
        }
        return table.toString();
    }

    private void addSessionToList(ArrayList<SessionID> list, SessionID session) {
        if (!list.contains(session)) {
            list.add(session);
        }
    }

    private void removeSessionFromList(ArrayList<SessionID> list, SessionID session) {
        int index = list.indexOf(session);
        if (index >= 0) {
            list.remove(session);
        }
    }
}
