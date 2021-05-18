package com.ibyte.iot.tcp.connector.tcp.codec;

import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

public class ProtobufAdapter {

    private ProtobufDecoder decoder = new ProtobufDecoder(MessageBuf.JMTransfer.getDefaultInstance());
    private ProtobufEncoder encoder = new ProtobufEncoder();

    public ProtobufDecoder getDecoder() {
        return decoder;
    }

    public ProtobufEncoder getEncoder() {
        return encoder;
    }
}
