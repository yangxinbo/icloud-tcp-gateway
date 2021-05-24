package com.ibyte.iot.tcp.connector.api;

import com.ibyte.iot.tcp.connector.Session;
import com.ibyte.iot.tcp.connector.SessionManager;
import com.ibyte.iot.tcp.connector.api.listener.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Li.shangzhi on 17/1/10.
 */
public abstract class ExchangeSessionManager implements SessionManager {

    private final static Logger logger = LoggerFactory.getLogger(ExchangeSessionManager.class);

    protected List<SessionListener> sessionListeners = null;

    public void setSessionListeners(List<SessionListener> sessionListeners) {
        this.sessionListeners = sessionListeners;
    }

    /**
     * The set of currently active Sessions for this Manager, keyed by session
     * identifier.
     */
    protected Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * define timeout 5min
     */
    private int maxInactiveInterval = 5 * 60;

    @Override
    public synchronized void addSession(Session session) {
        if (null == session) {
            return;
        }
        sessions.put(session.getSessionId(), session);
        logger.debug("put a session " + session.getSessionId() + " to sessions!");
    }

    @Override
    public synchronized void updateSession(String sessionId) {
        Session session = sessions.get(sessionId);
        session.setLastAccessedTime(System.currentTimeMillis());
        sessions.put(sessionId, session);
    }

    /**
     * Remove this Session from the active Sessions for this Manager.
     */
    @Override
    public synchronized void removeSession(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("session is null!");
        }
        removeSession(session.getSessionId());
    }

    @Override
    public synchronized void removeSession(String sessionId) {
        sessions.remove(sessionId);
        logger.debug("remove the session " + sessionId + " from sessions!");
    }

    @Override
    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public Session[] getSessions() {
        return sessions.values().toArray(new Session[0]);
    }

    @Override
    public Set<String> getSessionKeys() {
        return sessions.keySet();
    }

    @Override
    public int getSessionCount() {
        return sessions.size();
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

}
