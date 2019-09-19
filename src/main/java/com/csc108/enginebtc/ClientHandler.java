
package com.csc108.enginebtc;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * Created by LI JT on 2019/9/12.
 * Description:
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    ChannelHandlerContext ctx;
    boolean ready = false;

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

    public boolean isReady() {
        return ready;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        ready = true;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext arg0, String msg) throws Exception {
        System.out.println(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }
}