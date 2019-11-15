//package com.csc108.enginebtc.mina;
//
//import org.apache.mina.core.service.IoAcceptor;
//import org.apache.mina.core.service.IoHandlerAdapter;
//import org.apache.mina.core.session.IdleStatus;
//import org.apache.mina.filter.codec.ProtocolCodecFilter;
//import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;
//import org.apache.mina.filter.logging.LogLevel;
//import org.apache.mina.filter.logging.LoggingFilter;
//import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//
///**
// * Admin service controller through which we can config transports, ip, and
// * service handler.
// *
// * @author LI JT
// */
//public class AdminServiceController {
//
//    private static final Logger logger = LoggerFactory.getLogger(AdminServiceController.class);
//
//    // Instance of service controller.
//    public static final AdminServiceController CONTROLLER = new AdminServiceController();
//
//    private static final String ADMIN_IP_PROPERTY_NAME = "admin.ip";
//    private static final String ADMIN_PORT_PROPERTY_NAME = "admin.port";
//
//    // Filter for logger.
//    private static final String FILTER_LOGGER = "logger";
//    // Filter for decoding.
//    private static final String FILTER_CODEC = "codec";
//    // Default Buffer size.
//    private static final int DEFAULT_BUFFER_SIZE = 8192;
//    // Default Idle time.
//    private static final int DEFAULT_IDLE_TIME = 1;
//
//    // Mina IoAcceptor.
//    private IoAcceptor acceptor = new NioSocketAcceptor();
//    // Io handler adapter.
//    private IoHandlerAdapter handler = AdminServiceIoHandler.HANDLER;
//    // Transport.
//    private int port;
//    // Ip.
//    private String ip;
//    // Buffer size
//    private int bufferSize = DEFAULT_BUFFER_SIZE;
//    // Idle time.
//    private int idleTime = DEFAULT_IDLE_TIME;
//
//    /**
//     * Constructor.
//     */
//    private AdminServiceController() {
//        logger.info("Starting Admin Service.");
//        loadConfig();
//    }
//
//    /**
//     * Initialize the Admin service.
//     */
//    public void start() {
//        LoggingFilter loggingFilter = new LoggingFilter();
//        disableLogs(loggingFilter);
//
//        acceptor.getFilterChain().addLast(FILTER_LOGGER, loggingFilter);
//        // Don't use the TextLineCodec now, since the admin.exe doesn't
//        // send us the return char.
//        DemuxingProtocolCodecFactory factory = new DemuxingProtocolCodecFactory();
//        factory.addMessageDecoder(new AdminCodec());
//        factory.addMessageEncoder(String.class, new AdminCodec());
//        ProtocolCodecFilter filter = new ProtocolCodecFilter(factory);
//        acceptor.getFilterChain().addLast(FILTER_CODEC, filter);
//        acceptor.setHandler(handler);
//
//        acceptor.getSessionConfig().setReadBufferSize(bufferSize);
//        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, idleTime);
//
//        try {
//            acceptor.bind(new InetSocketAddress(ip, port));
//        } catch (IOException exception) {
//            throw new RuntimeException("Cannot initialize admin services at address. [ip: "
//                    + ip + ", port: " + port + "]" + exception, exception);
//        }
//    }
//
//    /**
//     * Disable logs of Mina.
//     * @param loggingFilter Log filter.
//     */
//    private void disableLogs(LoggingFilter loggingFilter) {
//        loggingFilter.setSessionClosedLogLevel(LogLevel.NONE);
//        loggingFilter.setSessionCreatedLogLevel(LogLevel.NONE);
//        loggingFilter.setSessionOpenedLogLevel(LogLevel.NONE);
//        loggingFilter.setMessageReceivedLogLevel(LogLevel.NONE);
//        loggingFilter.setMessageSentLogLevel(LogLevel.NONE);
//    }
//
//    /**
//     * Set the transport of admin service.
//     * @param port transport of admin service.
//     */
//    public void setPort(int port) {
//        this.port = port;
//    }
//
//    /**
//     * Set the ip of admin service.
//     * @param ip ip of admin service
//     */
//    public void setIp(String ip) {
//        this.ip = ip;
//    }
//
//    /**
//     * Get transport of admin service.
//     * @return transport of admin service.
//     */
//    public int getPort() {
//        return this.port;
//    }
//
//
//    /**
//     * Get Ip of admin service.
//     * @return Ip of admin service
//     */
//    public String getIp() {
//        return this.ip;
//    }
//
//    /**
//     * Load configuration file.
//     */
//    public void loadConfig() {
//        ip = "10.101.195.9";
//        port = 9201;
//    }
//
//}
