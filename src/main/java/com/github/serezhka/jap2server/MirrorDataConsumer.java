package com.github.serezhka.jap2server;

@FunctionalInterface
public interface MirrorDataConsumer {

    void onData(byte[] data);
}
