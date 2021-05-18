package com.ibyte.iot.tcp.common;

import com.ibyte.iot.tcp.connector.Connection;
import com.ibyte.iot.tcp.connector.SessionManager;
import com.ibyte.iot.tcp.connector.api.listener.SessionListener;

/**
 * @author Li.shangzhi
 * @version 1.0
 * @FileName Endpoint.java
 * @Description:
 * @Date Jan 15, 2019 11:02:35 AM
 */
public interface Endpoint extends Node {

    /**
     * @param connection
     */
    void setConnection(Connection connection);

    Connection getConnection();

    /**
     * @param sessionId
     */
    void setSessionId(String sessionId);

    String getSessionId();

    /**
     * @param sessionManager
     */
    void setSessionManager(SessionManager sessionManager);

    SessionManager getSessionManager();

    /**
     * Add a session event listener to this component.
     */
    void addSessionListener(SessionListener listener);

    /**
     * Remove a session event listener from this component.
     */
    void removeSessionListener(SessionListener listener);
}
