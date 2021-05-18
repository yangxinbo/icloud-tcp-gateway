package com.ibyte.iot.tcp.connector.api;

import com.ibyte.iot.tcp.connector.Connection;
import com.ibyte.iot.tcp.connector.Session;

/**
 * Created by Li.shangzhi on 17/1/10.
 * <blog:lishangzhi.github.io>
 */
public abstract class ExchangeConnection<T> implements Connection<T> {

    protected Session session = null;
    protected String connectionId = null;

    protected volatile boolean close = false;

    /**
     * ms
     */
    protected int connectTimeout = 60 * 60 * 1000;

    public void fireError(RuntimeException e) {
        throw e;
    }

    public boolean isClosed() {
        return close;
    }

    @Override
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public Session getSession() {
        return session;
    }

}
