package com.csc108.enginebtc.mina;

/**
 * Created by LI JT on 2019/9/12.
 * Description:
 */
public class Application {


    public static void main(String[] args) throws InterruptedException {
        AdminServiceController.CONTROLLER.start();

        while (true) {
            Thread.sleep(10000);
        }
    }



//    Send
//    public static void main(String[] args) throws InterruptedException {
//
//        IoConnector connector = new NioSocketConnector();
//        connector.getSessionConfig().setReadBufferSize(2048);
//
//        connector.getFilterChain().addLast("logger", new LoggingFilter());
//        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
//
//        connector.setHandler(new MinaClientHandler("Hello Server"));
//        ConnectFuture future = connector.connect(new InetSocketAddress("10.101.195.9", 9101));
//        future.awaitUninterruptibly();
//
//        if (!future.isConnected())
//        {
//            return;
//        }
//        IoSession session = future.getSession();
//        WriteFuture future1 = session.write("aaa");
//        future1.awaitUninterruptibly();
//        future1 = session.write("bbb");
//        future1.awaitUninterruptibly();
//        future1 = session.write("ccc");
//        session.write("ddd");
//        future1.awaitUninterruptibly();
//
//        connector.dispose();
//
//    }


}
