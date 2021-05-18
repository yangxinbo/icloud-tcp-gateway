package com.ibyte.iot.test.server;

import com.ibyte.iot.tcp.connector.api.listener.LogSessionListener;
import com.ibyte.iot.tcp.connector.api.listener.SessionListener;
import com.ibyte.iot.tcp.connector.tcp.TcpConnector;
import com.ibyte.iot.tcp.connector.tcp.TcpSessionManager;
import com.ibyte.iot.tcp.connector.tcp.config.ServerTransportConfig;
import com.ibyte.iot.tcp.connector.tcp.server.TcpServer;
import com.ibyte.iot.tcp.invoke.ApiProxy;
import com.ibyte.iot.tcp.notify.NotifyProxy;
import com.ibyte.iot.tcp.remoting.TcpSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FileName    : com.ibyte.iot.test.server
 * Description :
 *
 * @author : Shanks
 * @version : 1.0
 * Create Date : 2021/5/18 13:40
 **/
@Component
public class NettyServer {

    @Bean
    public TcpServer tcpServer() throws Exception {
        TcpServer tcpServer = new TcpServer();
        tcpServer.setPort(2000);
        return tcpServer;
    }

    @Bean
    public LogSessionListener logSessionListener() {
        return new LogSessionListener();
    }

    @Bean
    @Autowired
    public TcpSessionManager tcpSessionManager(LogSessionListener logSessionListener) {
        List<SessionListener> sessionListeners = new ArrayList<>();
        sessionListeners.add(logSessionListener);
        TcpSessionManager tcpSessionManager = new TcpSessionManager();
        tcpSessionManager.setMaxInactiveInterval(500);
        tcpSessionManager.setSessionListeners(sessionListeners);
        return tcpSessionManager;
    }

    @Bean
    public TcpConnector tcpConnector() {
        return new TcpConnector();
    }

    @Bean
    public TcpSender tcpSender(TcpConnector tcpConnector) {
        return new TcpSender(tcpConnector);
    }

    @Autowired
    @Bean
    public ServerTransportConfig serverTransportConfig(TcpConnector tcpConnector, ApiProxy proxy, NotifyProxy notify) {
        return new ServerTransportConfig(tcpConnector, proxy, notify);
    }

    @Bean
    @Autowired
    public NotifyProxy notifyProxy(TcpConnector tcpConnector) {
        return new NotifyProxy(tcpConnector);
    }


    @Bean
    public TestSimpleProxy testSimpleProxy() {
        return new TestSimpleProxy();
    }


}
