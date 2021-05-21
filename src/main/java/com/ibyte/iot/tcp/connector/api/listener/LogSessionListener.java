package com.ibyte.iot.tcp.connector.api.listener;

import lombok.extern.slf4j.Slf4j;

/**
 * 一个日志监听器，它和tcpSessionManager关联，监听器必须事先 SessionListener
 */
@Slf4j
public class LogSessionListener implements SessionListener {

    @Override
    public void sessionCreated(SessionEvent se) {
        log.info("session " + se.getSession().getSessionId() + " have been created!");
    }

    @Override
    public void sessionDestroyed(SessionEvent se) {
        log.info("session " + se.getSession().getSessionId() + " have been destroyed!");
    }
}
