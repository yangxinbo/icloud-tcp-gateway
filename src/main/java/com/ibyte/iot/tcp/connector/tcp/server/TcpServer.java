package com.ibyte.iot.tcp.connector.tcp.server;

import com.ibyte.iot.tcp.connector.tcp.codec.ProtobufAdapter;
import com.ibyte.iot.tcp.connector.tcp.config.ServerTransportConfig;
import com.ibyte.iot.tcp.exception.InitErrorException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
public class TcpServer {

    private int port;
    private static final int BIZ_GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int BIZ_THREAD_SIZE = 4;

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    @Autowired
    private ServerTransportConfig serverConfig;

    @PostConstruct
    public void init() throws Exception {
        log.info("start tcp server ...");

        // 一个主线程组
        bossGroup = new NioEventLoopGroup(BIZ_GROUP_SIZE);
        // 一个工作线程组
        workerGroup = new NioEventLoopGroup(BIZ_THREAD_SIZE);

        // Server 服务启动
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ProtobufAdapter adapter = new ProtobufAdapter(serverConfig);
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                        pipeline.addLast("decoder", adapter.getDecoder());
                        pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                        pipeline.addLast("encoder", adapter.getEncoder());
                        pipeline.addLast("handler", new TcpServerHandler(serverConfig));
                    }
                })

                // 可选参数
                .childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);

        // 绑定接口，同步等待成功
        log.info("start tcp server at port[" + port + "].");
        ChannelFuture future = bootstrap.bind(port).sync();
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                log.info("Server have success bind to " + port);
            } else {
                log.error("Server fail bind to " + port);
                throw new InitErrorException("Server start fail !", channelFuture.cause());
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
