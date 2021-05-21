package com.ibyte.iot.tcp.notify;

import com.ibyte.iot.tcp.connector.tcp.TcpConnector;
import com.ibyte.iot.tcp.connector.tcp.codec.MessageBuf;
import com.ibyte.iot.tcp.constant.Constants;
import com.ibyte.iot.tcp.message.MessageWrapper;
import com.ibyte.iot.tcp.utils.ByteUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 发送通知到代理类
 */
@Slf4j
public class NotifyProxy {

    private TcpConnector tcpConnector;

    public NotifyProxy(TcpConnector tcpConnector) {
        this.tcpConnector = tcpConnector;
    }

    private final ConcurrentHashMap<Long, NotifyFuture> futureMap = new ConcurrentHashMap<Long, NotifyFuture>();

    public int notify(long seq, MessageWrapper wrapper, int timeout) throws Exception {
        try {
            NotifyFuture<Boolean> future = doSendAsync(seq, wrapper, timeout);
            if (future == null) {
                return Constants.NOTIFY_NO_SESSION;
            } else {
                return future.get(timeout, TimeUnit.MILLISECONDS) ? Constants.NOTIFY_SUCCESS : Constants.NOTIFY_FAILURE;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void reply(MessageBuf.JMTransfer message) throws Exception {
        try {
            long seq = message.getSeq();
            log.info("reply seq -> " + seq + ", message -> " + ByteUtils.bytesToHexString(message.toByteArray()));
            final NotifyFuture future = this.futureMap.get(seq);
            if (future != null) {
                future.setSuccess(true);
                futureMap.remove(seq);
                log.info("reply seq -> " + seq + " success.");
            } else {
                log.info("reply seq -> " + seq + " expire.");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private NotifyFuture doSendAsync(long seq, MessageWrapper wrapper, int timeout) throws Exception {
        if (wrapper == null) {
            throw new Exception("wrapper cannot be null.");
        }
        String sessionId = wrapper.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            throw new Exception("sessionId cannot be null.");
        }
        if (tcpConnector.exist(sessionId)) {
            // start.
            final NotifyFuture future = new NotifyFuture(timeout);
            this.futureMap.put(seq, future);

            log.info("notify seq -> " + seq + ", sessionId -> " + sessionId);
            tcpConnector.send(sessionId, wrapper.getBody());

            // 置为已发送
            future.setSentTime(System.currentTimeMillis());
            return future;
        } else {
            // tcpConnector not exist sessionId
            return null;
        }
    }
}
