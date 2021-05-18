package com.ibyte.iot.tcp.connector.tcp.server;

import com.ibyte.iot.tcp.connector.tcp.config.ServerTransportConfig;
import com.ibyte.iot.tcp.exception.InitErrorException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
public class TcpServer {

    @Autowired
    private ServerTransportConfig serverConfig;

    private int port;

    private static final int BIZ_GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int BIZ_THREAD_SIZE = 4;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZ_GROUP_SIZE);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(BIZ_THREAD_SIZE);

    @PostConstruct
    public void init() throws Exception {
        boolean flag = Boolean.FALSE;
        log.info("start tcp server ...");

        Class clazz = NioServerSocketChannel.class;
        // Server 服务启动
        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(clazz);
        bootstrap.childHandler(new ServerChannelInitializer(serverConfig));
        // 可选参数
        bootstrap.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);

        // 绑定接口，同步等待成功
        log.info("start tcp server at port[" + port + "].");
        ChannelFuture future = bootstrap.bind(port).sync();
        ChannelFuture channelFuture = future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("Server have success bind to " + port);
                } else {
                    log.error("Server fail bind to " + port);
                    throw new InitErrorException("Server start fail !", future.cause());
                }
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        log.info("shutdown tcp server ...");
        // 释放线程池资源
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        log.info("shutdown tcp server end.");
    }

    //------------------ set && get --------------------
    public void setServerConfig(ServerTransportConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
