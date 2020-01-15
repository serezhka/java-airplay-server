package com.github.serezhka.jap2s.vlcj;

import com.github.serezhka.jap2lib.AirPlayBonjour;
import com.github.serezhka.jap2s.receiver.AirTunesServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Slf4j
@SpringBootApplication
public class VLCJPlayerApp {

    private final AirTunesServer airTunesServer;
    private final AirPlayBonjour airPlayBonjour;

    @Value("${airplay.port}")
    private int airPlayPort;

    @Value("${airtunes.port}")
    private int airTunesPort;

    @Autowired
    public VLCJPlayerApp(AirTunesServer airTunesServer,
                         AirPlayBonjour airPlayBonjour) {
        this.airTunesServer = airTunesServer;
        this.airPlayBonjour = airPlayBonjour;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(VLCJPlayerApp.class)
                .headless(false)
                .run(args);
    }

    @PostConstruct
    private void postConstruct() throws Exception {
        airPlayBonjour.start(airPlayPort, airTunesPort);
        new Thread(airTunesServer).start();
        log.info("AirTunes server started!");
    }

    @PreDestroy
    private void preDestroy() throws IOException {
        airPlayBonjour.stop();
        log.info("AirTunes server stopped!");
    }
}
