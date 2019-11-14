package com.csc108.enginebtc.fix;

import com.csc108.enginebtc.commons.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.ClOrdID;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;
import quickfix.fix42.Message;
import quickfix.fix42.NewOrderSingle;

import java.util.Arrays;

/**
 * Created by LI JT on 2019/9/10.
 * Description:
 */
public class FixMSgSender {

    private static final Logger logger = LoggerFactory.getLogger(FixMSgSender.class);


    /**
     * Send fix message with no delay.
     * @param msg
     * @param sessionID
     */
    public static void sendNow(Message msg, SessionID sessionID) {
        try {
            if(sessionID == null || quickfix.Session.lookupSession(sessionID) == null || !quickfix.Session.lookupSession(sessionID).isLoggedOn()) {
                throw new SessionNotFound(String.format("Msg %s sent out failed due to the session %s not logged on!", msg.toString(), sessionID));
            }

            quickfix.fix42.Message.Header header = (quickfix.fix42.Message.Header) msg.getHeader();
            header.setField(header.getBeginString());
            header.setField(new SenderCompID(sessionID.getSenderCompID()));
            header.setField(new TargetCompID(sessionID.getTargetCompID()));

            quickfix.Session.sendToTarget(msg);
        } catch (Exception ex) {
            // TODO
            logger.error("Error sending msg: " + ex.getMessage());
            logger.error(Arrays.toString(ex.getStackTrace()));
        }
    }


//    public static void publishOrders(Order order, SessionID sessionID) {
//        NewOrderSingle newOrderSingle = new NewOrderSingle(
//            new ClOrdID()
//
//
//
//        );
//    }





}
