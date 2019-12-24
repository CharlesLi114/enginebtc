package com.csc108.enginebtc.amq;

import com.csc108.enginebtc.controller.Controller;
import com.csc108.enginebtc.exception.SyncErrorException;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Created by LI JT on 2019/12/24.
 * Description:
 */
public class ActiveMessageReceiver implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ActiveMessageReceiver.class);

    @Override
    public void onMessage(Message message) {
        try {
            ActiveMQMapMessage msg = (ActiveMQMapMessage)message;
            String calcId = msg.getString("CalcId");
            boolean isDataReady = msg.getBoolean("IsDataReady");
            if (isDataReady) {
                Controller.Controller.addDataReadyCalc();
            } else {
                String reason = msg.getString("Reason");
                logger.error("Calc fails to recover tdb data, for reason: " + reason);
                throw new SyncErrorException("Calc fails to recover tdb data, for reason: " + reason);
            }
        } catch (JMSException e) {
            logger.error("Calc response process failure: ", e.getMessage());
            throw new SyncErrorException("Calc response process failure: ", e);
        }
    }
}
