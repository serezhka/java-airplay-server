package com.github.serezhka.jap2server;

import com.github.serezhka.jap2lib.AirPlayBonjour;
import com.github.serezhka.jap2server.internal.AirTunesServer;

public class AirPlayServer {

    private final AirPlayBonjour airPlayBonjour;
    private final MirrorDataConsumer mirrorDataConsumer;
    private final AirTunesServer airTunesServer;

    private final String serverName;
    private final int airPlayPort;
    private final int airTunesPort;

    public AirPlayServer(String serverName, int airPlayPort, int airTunesPort,
                         MirrorDataConsumer mirrorDataConsumer) {
        this.serverName = serverName;
        airPlayBonjour = new AirPlayBonjour(serverName);
        this.airPlayPort = airPlayPort;
        this.airTunesPort = airTunesPort;
        this.mirrorDataConsumer = mirrorDataConsumer;
        airTunesServer = new AirTunesServer(airPlayPort, airTunesPort, mirrorDataConsumer);
    }

    public void start() throws Exception {
        airPlayBonjour.start(airPlayPort, airTunesPort);
        new Thread(airTunesServer).start();
    }

    public void stop() {
        airPlayBonjour.stop();
    }

    // TODO On client connected / disconnected
}
