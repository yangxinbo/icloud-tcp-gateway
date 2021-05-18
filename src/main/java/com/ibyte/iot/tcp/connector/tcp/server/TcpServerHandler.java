package com.ibyte.iot.tcp.connector.tcp.server;

import com.ibyte.iot.tcp.connector.tcp.TcpConnector;
import com.ibyte.iot.tcp.connector.tcp.codec.MessageBuf;
import com.ibyte.iot.tcp.connector.tcp.config.ServerTransportConfig;
import com.ibyte.iot.tcp.constant.Constants;
import com.ibyte.iot.tcp.invoke.ApiProxy;
import com.ibyte.iot.tcp.message.MessageWrapper;
import com.ibyte.iot.tcp.message.SystemMessage;
import com.ibyte.iot.tcp.notify.NotifyProxy;
import com.ibyte.iot.tcp.utils.NetUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

//public class TcpServerHandler extends ChannelHandlerAdapter {
@Slf4j
@ChannelHandler.Sharable
public class TcpServerHandler extends ChannelInboundHandlerAdapter {

    private TcpConnector tcpConnector = null;
    private ApiProxy proxy = null;
    private NotifyProxy notify = null;

    public TcpServerHandler(ServerTransportConfig config) {
        this.tcpConnector = config.getTcpConnector();
        this.proxy = config.getProxy();
        this.notify = config.getNotify();
    }

    /**
     * read
     *
     * @param ctx
     * @param o
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
        try {
            if (o instanceof MessageBuf.JMTransfer) {
                SystemMessage sMsg = generateSystemMessage(ctx);
                MessageBuf.JMTransfer message = (MessageBuf.JMTransfer) o;
                // inbound
                if (message.getFormat() == SEND) {
                    MessageWrapper wrapper = proxy.invoke(sMsg, message);
                    if (wrapper != null) {
                        this.receive(ctx, wrapper);
                    }
                }
                // outbound
                if (message.getFormat() == REPLY) {
                    notify.reply(message);
                }
            } else {
                log.warn("TcpServerHandler channelRead message is not proto.");
            }
        } catch (Exception e) {
            log.error("TcpServerHandler TcpServerHandler handler error.", e);
            throw e;
        }
    }

    /**
     * registered
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("TcpServerHandler Connected from {" + NetUtils.channelToString(ctx.channel().remoteAddress(), ctx.channel().localAddress()) + "}");
    }

    /**
     * unregistered
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("TcpServerHandler Disconnected from {" + NetUtils.channelToString(ctx.channel().remoteAddress(), ctx.channel().localAddress()) + "}");
    }

    /**
     * active
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.debug("TcpServerHandler channelActive from (" + getRemoteAddress(ctx) + ")");
    }

    /**
     * inactive
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.debug("TcpServerHandler channelInactive from (" + getRemoteAddress(ctx) + ")");
        String sessionId0 = getChannelSessionHook(ctx);
        if (StringUtils.isNotBlank(sessionId0)) {
            tcpConnector.close(new MessageWrapper(MessageWrapper.MessageProtocol.CLOSE, sessionId0, null));
            log.warn("TcpServerHandler channelInactive, close channel sessionId0 -> " + sessionId0 + ", ctx -> " + ctx.toString());
        }
    }

    /**
     * exception
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("TcpServerHandler (" + getRemoteAddress(ctx) + ") -> Unexpected exception from downstream." + cause);
        String sessionId0 = getChannelSessionHook(ctx);
        if (StringUtils.isNotBlank(sessionId0)) {
            log.error("TcpServerHandler exceptionCaught (sessionId0 -> " + sessionId0 + ", ctx -> " + ctx.toString() + ") -> Unexpected exception from downstream." + cause);
        }
    }

    private String getChannelSessionHook(ChannelHandlerContext ctx) {
        return ctx.channel().attr(Constants.SERVER_SESSION_HOOK).get();
    }

    private void setChannelSessionHook(ChannelHandlerContext ctx, String sessionId) {
        ctx.channel().attr(Constants.SERVER_SESSION_HOOK).set(sessionId);
    }

    final int SEND = 1;
    final int RECEIVE = 2;
    final int NOTIFY = 3;
    final int REPLY = 4;

    /**
     * to send client and receive the message
     *
     * @param ctx
     * @param wrapper
     */
    private void receive(ChannelHandlerContext ctx, MessageWrapper wrapper) {
        if (wrapper.isConnect()) {
            isConnect0(ctx, wrapper);
        } else if (wrapper.isClose()) {
            tcpConnector.close(wrapper);
        } else if (wrapper.isHeartbeat()) {
            tcpConnector.heartbeatClient(wrapper);
        } else if (wrapper.isSend()) {
            tcpConnector.responseSendMessage(wrapper);
        } else if (wrapper.isNoKeepAliveMessage()) {
            tcpConnector.responseNoKeepAliveMessage(ctx, wrapper);
        }
    }

    private void isConnect0(ChannelHandlerContext ctx, MessageWrapper wrapper) {
        String sessionId = wrapper.getSessionId();
        String localSessionId = getChannelSessionHook(ctx);
        if (sessionId.equals(localSessionId)) {
            log.info("tcpConnector reconnect sessionId -> " + sessionId + ", ctx -> " + ctx.toString());
            tcpConnector.responseSendMessage(wrapper);
        } else {
            log.info("tcpConnector connect sessionId -> " + sessionId + ", sessionId0 -> " + localSessionId + ", ctx -> " + ctx.toString());
            tcpConnector.connect(ctx, wrapper);
            setChannelSessionHook(ctx, sessionId);
            log.info("create channel attr sessionId " + sessionId + " successful, ctx -> " + ctx.toString());
        }
    }

    /**
     * 组织系统消息
     *
     * @param ctx
     * @return
     */
    private SystemMessage generateSystemMessage(ChannelHandlerContext ctx) {
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setRemoteAddress(getRemoteAddress(ctx));
        systemMessage.setLocalAddress(getLocalAddress(ctx));
        return systemMessage;
    }

    /**
     * 获取远程ip
     *
     * @param ctx
     * @return
     */
    private String getRemoteAddress(ChannelHandlerContext ctx) {
        SocketAddress remote1 = ctx.channel().remoteAddress();
        InetSocketAddress remote = (InetSocketAddress) remote1;
        return NetUtils.toAddressString(remote);
    }

    /**
     * 获取本地ip
     *
     * @param ctx
     * @return
     */
    private String getLocalAddress(ChannelHandlerContext ctx) {
        SocketAddress local1 = ctx.channel().localAddress();
        InetSocketAddress local = (InetSocketAddress) local1;
        return NetUtils.toAddressString(local);
    }
}
