package com.csc108.enginebtc.admin;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.security.PublicKey;

/**
 * Created by LI JT on 2019/9/19.
 * Description:
 */
public class SenderHandler extends SimpleChannelInboundHandler<String> {

    private ChannelHandlerContext ctx;
    private boolean ready = false;

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
    }

    public boolean isReady() {
        return ready;
    }



    /**
     * <strong>Please keep in mind that this method will be renamed to
     * {@code messageReceived(ChannelHandlerContext, I)} in 5.0.</strong>
     * <p>
     * Is called for each message of type {@link I}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Inactive");
        this.ready = false;
    }
}
