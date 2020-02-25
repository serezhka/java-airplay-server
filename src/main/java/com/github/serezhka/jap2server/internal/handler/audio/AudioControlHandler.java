package com.github.serezhka.jap2server.internal.handler.audio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioControlHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger log = LoggerFactory.getLogger(AudioControlHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        ByteBuf content = msg.content();
        int contentLength = content.readableBytes();
        byte[] contentBytes = new byte[contentLength];
        content.readBytes(contentBytes);
        int type = contentBytes[1] & ~0x80;
        log.debug("Got audio control packet, type: {}, length: {}", type, contentLength);
    }
}
