package com.github.serezhka.jap2s.h264dump;

import com.github.serezhka.jap2lib.AirPlayBonjour;
import com.github.serezhka.jap2s.receiver.AirTunesServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Slf4j
@SpringBootApplication
public class H264DumpApp {

    private final AirTunesServer airTunesServer;
    private final AirPlayBonjour airPlayBonjour;
    private final H264Dump h264Dump;

    @Value("${airplay.port}")
    private int airPlayPort;

    @Value("${airtunes.port}")
    private int airTunesPort;

    @Autowired
    public H264DumpApp(AirTunesServer airTunesServer,
                       AirPlayBonjour airPlayBonjour,
                       H264Dump h264Dump) {
        this.airTunesServer = airTunesServer;
        this.airPlayBonjour = airPlayBonjour;
        this.h264Dump = h264Dump;
    }

    public static void main(String[] args) {
        SpringApplication.run(H264DumpApp.class, args);
    }

    @PostConstruct
    private void postConstruct() throws Exception {
        airPlayBonjour.start(airPlayPort, airTunesPort);
        new Thread(airTunesServer).start();
        log.info("AirTunes server started!");
    }

    @PreDestroy
    private void preDestroy() throws IOException {
        h264Dump.save();
        airPlayBonjour.stop();
        log.info("AirTunes server stopped!");
    }
}
