package com.github.serezhka.jap2s.receiver.handler.mirroring;

@FunctionalInterface
public interface MirrorDataConsumer {

    void onData(byte[] data);
}
