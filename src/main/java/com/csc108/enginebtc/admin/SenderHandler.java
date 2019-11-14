package com.csc108.enginebtc.admin;

import com.csc108.enginebtc.controller.Controller;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.security.PublicKey;

/**
 * Created by LI JT on 2019/9/19.
 * Description:
 */
public class SenderHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(SenderHandler.class);


    private ChannelHandlerContext ctx;
    private boolean ready = false;
    private NettySender sender;
    private boolean liveBeforeResponse;

    public SenderHandler(NettySender sender, boolean liveBeforeResponse) {
        this.sender = sender;
        this.liveBeforeResponse = liveBeforeResponse;
    }

    public void sendMessage(String msgToSend) {
        if (ctx != null) {
            ChannelFuture cf = ctx.write(Unpooled.copiedBuffer(msgToSend, CharsetUtil.UTF_8));
            ctx.flush();
            cf.awaitUninterruptibly();
            if (!cf.isSuccess()) {
                System.out.println("Send failed: " + cf.cause());
            } else {
                System.out.println("OK");
            }
        } else {
            //ctx not initialized yet. you were too fast. do something here
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        ready = true;
        logger.info(sender.getConfig() + " Connected.");
    }

    public boolean isReady() {
        return ready;
    }


    /**
     * <strong>Please keep in mind that this method will be renamed to
     * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
     * <p>
     * Is called for each message of type {@link }.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("Send channel receives response:");
        System.out.println(msg);
        if (!StringUtils.isBlank(msg) && liveBeforeResponse) {
            msg = msg.replaceAll("[\n\r]", "");
            logger.info("ReceivedMsg: \n" + msg);

            if (msg.equalsIgnoreCase("TdbDataRecovered.")) {
                Controller.Controller.setCalcDataReady();
                logger.info("Calc " + this.sender.getConfig() + " data recovered.");
                this.sender.stop();
            }
            if (msg.startsWith("OffsetDone")) {
                Controller.Controller.setCalcTimeReady();
                logger.info("Calc " + this.sender.getConfig() + " offset done.");
                this.sender.stop();
            }
            if (msg.startsWith("OrderProcessorCount")) {
                Controller.Controller.setEngineTimeReady();
                logger.info("Engine "+ this.sender.getConfig() + " offset done.");
                this.sender.stop();
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(this.sender.getConfig() + " Inactive");
        logger.info(this.sender.getConfig() + " Inactive");
        this.ready = false;
    }
}
