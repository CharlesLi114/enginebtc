
        package com.csc108.enginebtc;

        import io.netty.bootstrap.Bootstrap;
        import io.netty.channel.*;
        import io.netty.channel.nio.NioEventLoopGroup;
        import io.netty.channel.socket.SocketChannel;
        import io.netty.channel.socket.nio.NioSocketChannel;
        import io.netty.handler.codec.DelimiterBasedFrameDecoder;
        import io.netty.handler.codec.Delimiters;
        import io.netty.handler.codec.string.StringDecoder;
        import io.netty.handler.codec.string.StringEncoder;

        import java.util.concurrent.ExecutorService;
        import java.util.concurrent.Executors;
        import java.util.concurrent.TimeUnit;

/**
 * Created by LI JT on 2019/9/12.
 * Description:
 */
public class Client implements Runnable {

    String host = "10.101.237.68";
    int port = 9201;
    private final ClientHandler clientHandler = new ClientHandler();
    private boolean isRunning = false;
    private ExecutorService executor = null;


    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();
        client.startClient();

        while (!client.clientHandler.isReady()) {
            Thread.sleep(1000);
            System.out.println("Wait for connection.");
        }
        client.writeMessage("random_text");
        client.writeMessage("random_text1");
        client.writeMessage("random_text2");
        client.writeMessage("random_text3");
        client.writeMessage("Fuck it.");
        //client.stopClient();  //call this at some point to shutdown the client
    }

    public synchronized void startClient() {
        if (!isRunning) {
            executor = Executors.newFixedThreadPool(1);
            executor.execute(this);
            isRunning = true;
        }
    }

    public synchronized boolean stopClient() {
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
                    pipeline.addLast(clientHandler);
                }
            });

            ChannelFuture f = b.connect(host, port).sync();

            f.channel().closeFuture().sync();
        } catch (InterruptedException ex) {
            // do nothing
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void writeMessage(String msg) {
        clientHandler.sendMessage(msg);
    }
}