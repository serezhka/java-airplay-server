package com.github.serezhka.jap2s.h264dump;

import com.github.serezhka.jap2s.receiver.handler.mirroring.MirrorDataConsumer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class H264Dump implements MirrorDataConsumer {

    private final FileChannel fc;

    public H264Dump(String dumpName) throws IOException {
        fc = FileChannel.open(Paths.get(dumpName), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    @Override
    public void onData(byte[] data) {
        try {
            fc.write(ByteBuffer.wrap(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() throws IOException {
        fc.close();
    }
}
