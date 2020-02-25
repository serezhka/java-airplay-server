package com.github.serezhka.jap2server.internal.handler.audio;

import com.github.serezhka.jap2lib.AirPlay;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AudioHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger log = LoggerFactory.getLogger(AudioHandler.class);

    private final AirPlay airPlay;

    public AudioHandler(AirPlay airPlay) throws IOException {
        this.airPlay = airPlay;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws IOException {
        ByteBuf content = msg.content();
        int contentLength = content.readableBytes();
        byte[] contentBytes = new byte[contentLength];
        content.readBytes(contentBytes);

        int flag = contentBytes[0] & 0xFF;
        int type = contentBytes[1] & 0x7F;
        log.debug("Got audio packet. flag: {}, type: {}, length: {}", flag, type, contentLength);

        if (type == 96 || type == 86) {
            int off = 0;
            if (type == 86) {
                off = 4;
            }

            int curSeqNo = ((contentBytes[off + 2] & 0xFF) << 8) | (contentBytes[off + 3] & 0xFF);
            log.debug("Current sequence number: " + curSeqNo);

            long timestamp = (contentBytes[off + 7] & 0xFF) | ((contentBytes[off + 6] & 0xFF) << 8) | ((contentBytes[off + 5] & 0xFF) << 16) | ((contentBytes[off + 4] & 0xFF) << 24);
            log.debug("Timestamp: " + timestamp);

            long ssrc = (contentBytes[off + 11] & 0xFF) | ((contentBytes[off + 6] & 0xFF) << 8) | ((contentBytes[off + 9] & 0xFF) << 16) | ((contentBytes[off + 8] & 0xFF) << 24);
            log.debug("SSRC: " + ssrc);

            /*if (contentLength > 16) {
                try {
                    byte[] tmp = new byte[contentLength - 12];
                    System.arraycopy(contentBytes, 12, tmp, 0, tmp.length);
                    airPlay.fairPlayDecryptAudioData(tmp);
                    //fc.write(ByteBuffer.wrap(tmp));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
        }
    }
}
