package com.github.serezhka.jap2server;

import com.github.serezhka.jap2lib.AirPlayBonjour;
import com.github.serezhka.jap2server.internal.ControlServer;

public class AirPlayServer {

    private final AirPlayBonjour airPlayBonjour;
    private final AirplayDataConsumer airplayDataConsumer;
    private final ControlServer controlServer;

    private final String serverName;
    private final int airPlayPort;
    private final int airTunesPort;

    public AirPlayServer(String serverName, int airPlayPort, int airTunesPort,
                         AirplayDataConsumer airplayDataConsumer) {
        this.serverName = serverName;
        airPlayBonjour = new AirPlayBonjour(serverName);
        this.airPlayPort = airPlayPort;
        this.airTunesPort = airTunesPort;
        this.airplayDataConsumer = airplayDataConsumer;
        controlServer = new ControlServer(airPlayPort, airTunesPort, airplayDataConsumer);
    }

    public void start() throws Exception {
        airPlayBonjour.start(airPlayPort, airTunesPort);
        new Thread(controlServer).start();
    }

    public void stop() {
        airPlayBonjour.stop();
    }

    // TODO On client connected / disconnected
}
