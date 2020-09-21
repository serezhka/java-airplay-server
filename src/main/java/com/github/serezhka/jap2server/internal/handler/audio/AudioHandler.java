package com.github.serezhka.jap2server.internal.handler.audio;

import com.github.serezhka.jap2lib.AirPlay;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.TreeMap;

public class AudioHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger log = LoggerFactory.getLogger(AudioHandler.class);

    private static boolean aacDecoderInitialized = false;

    static {
        try {
            System.loadLibrary("libfdk-aac");
            aacDecoderInitialized = true;
        } catch (UnsatisfiedLinkError e) {
            log.warn("libfdk-aac.dll not found!");
        }
    }

    private final AirPlay airPlay;

    private final Map<Integer, byte[]> buffer = new TreeMap<>();

    private FileChannel fc;
    private int prevSeqNum;
    private boolean synced;

    public AudioHandler(AirPlay airPlay) throws IOException {
        this.airPlay = airPlay;
        if (aacDecoderInitialized) {
            fc = FileChannel.open(Paths.get("audio.pcm"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            init();
        }
    }

    public native void init();

    public native void decodeFrame(byte[] input, byte[] output);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf content = msg.content();
        int contentLength = content.readableBytes();
        byte[] contentBytes = new byte[contentLength];
        content.readBytes(contentBytes);

        int flag = contentBytes[0] & 0xFF;
        int type = contentBytes[1] & 0x7F;

        // Version (V) is 2 and the payload type (PT) is 96 (DynamicRTP-Type-96).

        log.debug("Got audio packet. flag: {}, type: {}, length: {}", flag, type, contentLength);

        if (!aacDecoderInitialized) {
            return;
        }

        if (type == 96 || type == 86) {
            int off = 0;
            if (type == 86) {
                off = 4;
            }

            int curSeqNo = ((contentBytes[off + 2] & 0xFF) << 8) | (contentBytes[off + 3] & 0xFF);
            //log.debug("Current sequence number: " + curSeqNo);

            long timestamp = (contentBytes[off + 7] & 0xFF) | ((contentBytes[off + 6] & 0xFF) << 8) | ((contentBytes[off + 5] & 0xFF) << 16) | ((contentBytes[off + 4] & 0xFF) << 24);
            //log.debug("Timestamp: " + timestamp);

            long ssrc = (contentBytes[off + 11] & 0xFF) | ((contentBytes[off + 6] & 0xFF) << 8) | ((contentBytes[off + 9] & 0xFF) << 16) | ((contentBytes[off + 8] & 0xFF) << 24);
            //log.debug("SSRC: " + ssrc);

            if (contentLength > 16) {
                byte[] audio = new byte[contentLength - 12];
                System.arraycopy(contentBytes, 12, audio, 0, audio.length);
                airPlay.fairPlayDecryptAudioData(audio);

                if (prevSeqNum == 0) {
                    prevSeqNum = curSeqNo;
                    synced = true;

                    byte[] audioDecoded = new byte[480 * 4];
                    decodeFrame(audio, audioDecoded);

                    fc.write(ByteBuffer.wrap(audioDecoded));
                } else {

                    if (curSeqNo - prevSeqNum == 1) {
                        prevSeqNum = curSeqNo;
                        synced = true;

                        byte[] audioDecoded = new byte[480 * 4];
                        decodeFrame(audio, audioDecoded);

                        fc.write(ByteBuffer.wrap(audioDecoded));
                    } else if (curSeqNo > prevSeqNum) {

                        synced = false;

                        byte[] audioDecoded = new byte[480 * 4];
                        decodeFrame(audio, audioDecoded);

                        buffer.put(curSeqNo, audioDecoded);
                    }
                }
            }

            if (!synced) {
                while (buffer.containsKey(prevSeqNum + 1)) {
                    fc.write(ByteBuffer.wrap(buffer.remove(prevSeqNum + 1)));
                    prevSeqNum++;
                }
                synced = buffer.isEmpty();
            }
        }
    }
}
