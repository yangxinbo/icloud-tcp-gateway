package com.ibyte.iot.tcp.connector.api.listener;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Li.shangzhi on 17/1/10.
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
