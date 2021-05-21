package com.ibyte.iot.tcp.connector.tcp;

import com.ibyte.iot.tcp.connector.Session;
import com.ibyte.iot.tcp.connector.tcp.listener.TcpHeartbeatListener;
import com.ibyte.iot.tcp.message.MessageWrapper;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


/**
 * TCP 容器,用于管理服务和客户端的连接
 */
@Slf4j
public class TcpConnector extends ExchangeTcpConnector {

    private TcpHeartbeatListener tcpHeartbeatListener = null;

    @Override
    @PostConstruct
    public void init() {
        tcpHeartbeatListener = new TcpHeartbeatListener(tcpSessionManager);
        Thread heartbeatThread = new Thread(tcpHeartbeatListener, "tcpHeartbeatListener");
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }


    @Override
    @PreDestroy
    public void destroy() {
        tcpHeartbeatListener.stop();
        for (Session session : tcpSessionManager.getSessions()) {
            session.close();
        }
        tcpSessionManager = null;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, MessageWrapper wrapper) {
        try {
            Session session = tcpSessionManager.createSession(wrapper.getSessionId(), ctx);
            session.addSessionListener(tcpHeartbeatListener);
            session.connect();

            tcpSessionManager.addSession(session);
            /** send **/
            session.getConnection().send(wrapper.getBody());
        } catch (Exception e) {
            log.error("TcpConnector connect occur Exception.", e);
        }
    }

    @Override
    public void close(MessageWrapper wrapper) {
        Session session = tcpSessionManager.getSession(wrapper.getSessionId());
        session.getConnection().send(wrapper.getBody());
        session.close();
    }

    @Override
    public void heartbeatClient(MessageWrapper wrapper) {
        try {
            tcpSessionManager.updateSession(wrapper.getSessionId());
            Session session = tcpSessionManager.getSession(wrapper.getSessionId());
            session.getConnection().send(wrapper.getBody());
        } catch (Exception e) {
            log.error("TcpConnector heartbeatClient occur Exception.", e);
        }
    }

    @Override
    public void responseSendMessage(MessageWrapper wrapper) {
        try {
            Session session = tcpSessionManager.getSession(wrapper.getSessionId());
            session.getConnection().send(wrapper.getBody());
        } catch (Exception e) {
            log.error("TcpConnector responseSendMessage occur Exception.", e);
        }
    }

    @Override
    public void responseNoKeepAliveMessage(ChannelHandlerContext ctx, MessageWrapper wrapper) {
        try {
            NoKeepAliveTcpConnection connection = new NoKeepAliveTcpConnection(ctx);
            connection.send(wrapper.getBody());
        } catch (Exception e) {
            log.error("TcpConnector responseNoKeepAliveMessage occur Exception.", e);
        }
    }
}
