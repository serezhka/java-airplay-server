package com.github.serezhka.jap2s.h264dump;

import com.github.serezhka.jap2lib.AirPlayBonjour;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.io.IOException;

@Configuration
@PropertySource("classpath:h264-dump.properties")
@ComponentScan("com.github.serezhka.jap2s")
public class H264DumpConfig {

    @Bean
    public static H264Dump mirrorDataConsumer(@Value("${dump.name}") String dumpName) throws IOException {
        return new H264Dump(dumpName);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public static AirPlayBonjour airPlayBonjour(@Value("${server.name}") String serverName) {
        return new AirPlayBonjour(serverName);
    }
}