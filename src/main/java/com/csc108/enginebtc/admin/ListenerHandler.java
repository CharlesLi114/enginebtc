package com.csc108.enginebtc.admin;

import com.csc108.enginebtc.controller.Controller;
import com.csc108.enginebtc.utils.Constants;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by LI JT on 2017/12/1.
 * Description:
 */
public class ListenerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ListenerHandler.class);

    private static final String ErrorMsg = "Input message %s is not supported.";



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String message = (String) msg;
        logger.info("Received message " + message);
        if (message.trim().toUpperCase().equals(Constants.CalcReadyMsg)) {
            Controller.Controller.setCalcReady();
        }


        System.out.println(message);
        if (message.trim().equalsIgnoreCase("AAA")) {
            System.out.println("LIKESHIT");
        }
        logger.info("In Msg:\n" + message);

//        AbstractCommand targetCmd = null;
//        List<AbstractCommand> commands = NettyListener.Netty.getCommands();
//        for (AbstractCommand command : commands) {
//            if (command.matchThirdCmd(message)) {
//                targetCmd = command;
//                break;
//            }
//        }
//        if (targetCmd == null) {
//            StringBuilder retMsg = new StringBuilder();
//            for (AbstractCommand command : commands) {
//                if (command.matchSecondCmd(message)) {
//                    retMsg.append(command.getCommand()).append(Constants.New_Line);
//                }
//            }
//            if (StringUtils.isBlank(retMsg.toString())) {
//                for (AbstractCommand command : commands) {
//                    if (command.matchFirstCmd(message)) {
//                        retMsg.append(command.getCommand()).append(Constants.New_Line);
//                    }
//                }
//            }
//            if (StringUtils.isBlank(retMsg.toString())) {
//                Set<String> cmds = new HashSet<>();
//                for (AbstractCommand command : commands) {
//                    cmds.add(command.getBaseCmd());
//                }
//                retMsg.append("Admin commands :").append(Constants.New_Line);
//                for (String baseMsg : cmds) {
//                    retMsg.append(baseMsg).append(Constants.New_Line);
//                }
//            }
//            writeNClose(ctx, retMsg.toString());
//        } else {
//            targetCmd.process(message, ctx);
//        }

    }

    public static void writeNClose(ChannelHandlerContext ctx, String msg) {
        logger.info("Out Msg:\n" + msg);
        ChannelFuture future = ctx.writeAndFlush(msg + "\n\r");
        future.addListener(ChannelFutureListener.CLOSE);
    }


}
