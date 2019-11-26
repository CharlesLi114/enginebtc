package com.csc108.enginebtc.admin;

import com.csc108.enginebtc.commons.AbstractLifeCircleBean;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.utils.ConfigUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by LI JT on 2019/9/12.
 * Description:
 */
public class NettyListener extends AbstractLifeCircleBean {

    private static final Logger logger = LoggerFactory.getLogger(NettyListener.class);

    private static final String PROPERTY_CONFIG_FILE = "application.properties";
    private static final String Receiver_IP_Property_Name = "admin.receive.ip";
    private static final String Receiver_Port_Property_Name = "admin.receive.port";




    public static final NettyListener Netty = new NettyListener();


    private AdminConfig ownConfig;


    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;


    @Override
    public void config() {
        String configFile = ConfigUtil.getConfigPath(PROPERTY_CONFIG_FILE);
        try {
            Configuration config = new PropertiesConfiguration(configFile);
            String ip = config.getString(Receiver_IP_Property_Name);
            int port = config.getInt(Receiver_Port_Property_Name);
            this.ownConfig = new AdminConfig(ip, port);

        } catch (ConfigurationException e) {
            logger.error("Failed to read config for NettyListener", e);
            throw new InitializationException("Failed to read config for NettyListener", e);
        }
    }

    @Override
    public void start() {
        this.config();
        this.startListener();
    }



    private void startListener() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new ListenerHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            b.bind(ownConfig.getPort()).sync();
        } catch (InterruptedException e) {
            logger.error("Start up Netty Server failure.", e);
            throw new InitializationException("Failed to start socket listener.", e);
        }
        System.out.println("Started.");
    }

    @Override
    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }
}
