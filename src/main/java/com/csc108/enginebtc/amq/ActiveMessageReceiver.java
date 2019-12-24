package com.csc108.enginebtc.amq;

import com.csc108.enginebtc.controller.Controller;
import org.apache.activemq.command.ActiveMQMapMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Created by LI JT on 2019/12/24.
 * Description:
 */
public class ActiveMessageReceiver implements MessageListener {
    @Override
    public void onMessage(Message message) {
        try {
            ActiveMQMapMessage msg = (ActiveMQMapMessage)message;
            String calcId = msg.getString("CalcId");
            boolean isDataReady = msg.getBoolean("IsDataReady");
            if (isDataReady) {
                Controller.Controller.addDataReadyCalc();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
