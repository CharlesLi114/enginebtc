//package com.csc108.enginebtc.mina;
//
//import org.apache.mina.core.service.IoHandlerAdapter;
//import org.apache.mina.core.session.IdleStatus;
//import org.apache.mina.core.session.IoSession;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Handler for handling admin requests. This handler doesn't work on the handling process
// * but put an admin event to disruptor, and the admin event will be processed by disruptors.
// *
// * @author LI JT
// */
//public class AdminServiceIoHandler extends IoHandlerAdapter {
//
//    // Instance of admin service handler.
//    public static final AdminServiceIoHandler HANDLER = new AdminServiceIoHandler();
//
//    // DisruptorController logger.
//    private static final Logger logger = LoggerFactory.getLogger(AdminServiceIoHandler.class);
//
//    /**
//     * Private constructor.
//     */
//    private AdminServiceIoHandler() {
//    }
//
//    @Override
//    public void messageReceived(IoSession ioSession, Object message) {
//
//
//        System.out.println(message.toString());
//        if (logger.isInfoEnabled()) {
//            logger.info("Message received : " + message + "; from " + ioSession.getRemoteAddress().toString());
//        }
//
//        String parameters = message.toString();
//
//    }
//
//    @SuppressWarnings("deprecation")
//    public void sessionIdle(IoSession session, IdleStatus status) {
//        session.close();
//    }
//}
