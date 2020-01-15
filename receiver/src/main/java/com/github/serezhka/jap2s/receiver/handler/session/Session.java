package com.github.serezhka.jap2s.receiver.handler.session;

import com.github.serezhka.jap2lib.AirPlay;
import lombok.Getter;

@Getter
public class Session {

    private final AirPlay airPlay;
    private Thread airPlayReceiverThread;

    public Session() {airPlay = new AirPlay();}

    public Thread getAirPlayReceiverThread() {
        return airPlayReceiverThread;
    }

    public void setAirPlayReceiverThread(Thread airPlayReceiverThread) {
        this.airPlayReceiverThread = airPlayReceiverThread;
    }
}
