package com.csc108.enginebtc.admin;

import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.utils.ConfigUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by LI JT on 2019/9/19.
 * Description:
 */
public class NettySender implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NettySender.class);


    private static final String PROPERTY_CONFIG_FILE = "application.properties";
    private static final String SendTo_IP_Property_Name = "admin.sendto.ip";
    private static final String SendTo_Port_Property_Name = "admin.sendto.port";



    private SenderHandler handler;
    private boolean isRunning = false;
    private ExecutorService executor = null;

    private AdminConfig sendConfig;
    private boolean liveBeforeResponse;
    private String config;


    public NettySender(boolean liveBeforeResponse, String config) {
        this.handler = new SenderHandler(this, liveBeforeResponse);
        this.liveBeforeResponse = liveBeforeResponse;
        this.config = config;
    }

    public void config(String ip, int port) {
        if (ip == null) {
            String configFile = ConfigUtil.getConfigPath(PROPERTY_CONFIG_FILE);
            Configuration config = null;
            try {
                config = new PropertiesConfiguration(configFile);
            } catch (ConfigurationException e) {
                logger.error("Failed to read config for NettyListener", e);
                throw new InitializationException("Failed to read config for NettyListener", e);
            }
            ip = config.getString(SendTo_IP_Property_Name);
            port = config.getInt(SendTo_Port_Property_Name);
            this.sendConfig = new AdminConfig(ip, port);
        } else {
            this.sendConfig = new AdminConfig(ip, port);
        }
    }

    public boolean isReady() {
        return this.handler.isReady();
    }

    public void start() {
        if (!isRunning) {
            executor = Executors.newFixedThreadPool(1);
            executor.execute(this);
            isRunning = true;
        }
    }

    public boolean stop() {
        logger.info("Shut down netty connection " + this.config);
        boolean bReturn = true;
        if (isRunning) {
            if (executor != null) {
                executor.shutdown();
                try {
                    executor.shutdownNow();
                    if (executor.awaitTermination(calcTime(10, 0.66667), TimeUnit.SECONDS)) {
                        if (!executor.awaitTermination(calcTime(10, 0.33334), TimeUnit.SECONDS)) {
                            bReturn = false;
                        }
                    }
                } catch (InterruptedException ie) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                } finally {
                    executor = null;
                }
            }
            isRunning = false;
        }
        return bReturn;
    }

    private long calcTime(int nTime, double dValue) {
        return (long) ((double) nTime * dValue);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                    pipeline.addLast(new StringDecoder());
                    pipeline.addLast(new StringEncoder());
                    pipeline.addLast(handler);
                }
            });

            ChannelFuture f = b.connect(this.sendConfig.getIp(), this.sendConfig.getPort()).sync();

            f.channel().closeFuture().sync();
        } catch (InterruptedException ex) {
            // do nothing
            System.out.println("interruptted.");
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void writeMessage(String msg) {
        handler.sendMessage(msg);
    }

    public String getConfig() {
        return this.config;
    }



}
