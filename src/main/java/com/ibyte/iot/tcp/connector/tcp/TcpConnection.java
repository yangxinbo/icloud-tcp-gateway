package com.ibyte.iot.tcp.connector.tcp;

import com.ibyte.iot.tcp.connector.api.ExchangeConnection;
import com.ibyte.iot.tcp.exception.LostConnectException;
import com.ibyte.iot.tcp.exception.PushException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TcpConnection<T> extends ExchangeConnection<T> {

    private ChannelHandlerContext cxt;

    public TcpConnection(ChannelHandlerContext cxt) {
        this.cxt = cxt;
    }

    @Override
    public void connect() {
    }

    @Override
    public void close() {
        this.close = true;
        cxt.close();
        log.debug("the connection have been destroyed! ctx -> " + cxt.toString());
    }

    @Override
    public void send(T message) {
        if (message == null) {
            return;
        }
        sendMessage(message);
    }

    private void sendMessage(T message) {
        if (isClosed()) {
            PushException e = new PushException("Use a closed pushSocked!");
            this.fireError(e);
            return;
        }

        try {
            ChannelFuture cf = cxt.writeAndFlush(message);
            cf.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.debug("send success.");
                } else {
                    throw new PushException("Failed to send message.");
                }
                Throwable cause = future.cause();
                if (cause != null) {
                    throw new PushException(cause);
                }
            });
        } catch (LostConnectException e) {
            log.error("TcpConnection pushMessage occur LostConnectException.", e);
            this.fireError(new PushException(e));
        } catch (Exception e) {
            log.error("TcpConnection pushMessage occur Exception.", e);
            this.fireError(new PushException("ChannelFuture " + connectionId + " ", e));
        } catch (Throwable e) {
            log.error("TcpConnection pushMessage occur Throwable.", e);
            this.fireError(new PushException("Failed to send message, cause: " + e.getMessage(), e));
        }
    }


    private void pushMessage(T message) {
        boolean success = true;
        boolean sent = true;
        int timeout = 60;
        try {
            ChannelFuture cf = cxt.write(message);
            cxt.flush();
            if (sent) {
                success = cf.await(timeout);
            }
            if (cf.isSuccess()) {
                log.debug("send success.");
            }
            Throwable cause = cf.cause();
            if (cause != null) {
                this.fireError(new PushException(cause));
            }
        } catch (LostConnectException e) {
            log.error("TcpConnection pushMessage occur LostConnectException.", e);
            this.fireError(new PushException(e));
        } catch (Exception e) {
            log.error("TcpConnection pushMessage occur Exception.", e);
            this.fireError(new PushException("ChannelFuture " + connectionId + " ", e));
        } catch (Throwable e) {
            log.error("TcpConnection pushMessage occur Throwable.", e);
            this.fireError(new PushException("Failed to send message, cause: " + e.getMessage(), e));
        }
        if (!success) {
            this.fireError(new PushException("Failed to send message, in timeout(" + timeout + "ms) limit"));
        }
    }
}
