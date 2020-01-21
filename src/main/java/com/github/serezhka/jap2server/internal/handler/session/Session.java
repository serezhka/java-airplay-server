package com.github.serezhka.jap2server.internal.handler.session;

import com.github.serezhka.jap2lib.AirPlay;

public class Session {

    private final AirPlay airPlay;
    private Thread airPlayReceiverThread;

    Session() {airPlay = new AirPlay();}

    public AirPlay getAirPlay() {
        return airPlay;
    }

    public Thread getAirPlayReceiverThread() {
        return airPlayReceiverThread;
    }

    public void setAirPlayReceiverThread(Thread airPlayReceiverThread) {
        this.airPlayReceiverThread = airPlayReceiverThread;
    }
}
