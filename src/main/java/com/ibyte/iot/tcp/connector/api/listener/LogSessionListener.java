package com.ibyte.iot.tcp.connector.api.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Li.shangzhi on 17/1/10.
 */
public class LogSessionListener implements SessionListener {

    private final static Logger logger = LoggerFactory.getLogger(LogSessionListener.class);

    @Override
    public void sessionCreated(SessionEvent se) {
        logger.info("session " + se.getSession().getSessionId() + " have been created!");
    }

    @Override
    public void sessionDestroyed(SessionEvent se) {
        logger.info("session " + se.getSession().getSessionId() + " have been destroyed!");
    }
}
