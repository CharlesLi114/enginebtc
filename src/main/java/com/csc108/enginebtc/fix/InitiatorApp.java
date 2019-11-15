package com.csc108.enginebtc.fix;

import com.csc108.enginebtc.cache.FixSessionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.Message;
import quickfix.fix42.*;
import quickfix.fix42.MessageCracker;

/**
 * Created by LI JT on 2019/9/10.
 * Description:
 */
public class InitiatorApp extends MessageCracker implements Application {

    private static final Logger logger = LoggerFactory.getLogger(InitiatorApp.class);

    // TODO

    public void onCreate(SessionID sessionID) {

    }

    public void onLogon(SessionID sessionID) {
        logger.info("Session " + sessionID.toString() + " logged on.");
        FixSessionCache.getInstance().addSession(sessionID, false);
    }

    public void onLogout(SessionID sessionID) {
        logger.info("Session " + sessionID.toString() + " logged out.");
        FixSessionCache.getInstance().removeSession(sessionID);
    }

    public void toAdmin(Message message, SessionID sessionID) {

    }

    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    public void toApp(Message message, SessionID sessionID) throws DoNotSend {

    }

    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        System.out.println(message.toString());
        crack(message, sessionID);
    }

    public void onMessage(OrderPauseResumeRequest message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

    }

    public void onMessage(OrderCancelReject message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
//        FixUtil.handleCancelRejectedReport(message);
    }

    public void onMessage(ExecutionReport executionReport,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
//        FixUtil.handleExecutionReport(executionReport,sessionID);
    }

    public void onMessage(Reject reject, SessionID sessionID) {
//        FixUtil.handleReject(reject, sessionID);
    }
}
