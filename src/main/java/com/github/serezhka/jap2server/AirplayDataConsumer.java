package com.github.serezhka.jap2server;

public interface AirplayDataConsumer {

    void onVideo(byte[] video);

    void onAudio(byte[] audio);
}
