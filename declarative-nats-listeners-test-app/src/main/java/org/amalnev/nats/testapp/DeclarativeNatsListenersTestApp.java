package org.amalnev.nats.testapp;

import org.amalnev.nats.config.EnableJetStreamListeners;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableJetStreamListeners
@SpringBootApplication
public class DeclarativeNatsListenersTestApp {
    public static void main(String[] args) {
        SpringApplication.run(DeclarativeNatsListenersTestApp.class, args);
    }
}
