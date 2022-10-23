package org.amalnev.nats.testapp;

import io.nats.client.Connection;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class TestStartupConfiguration {

    static {
        GenericContainer<?> jetStreamContainer = new GenericContainer<>(DockerImageName.parse("nats:2.9.3-alpine3.16"))
                .withCommand("nats-server -js")
                .withExposedPorts(4222);
        jetStreamContainer.start();
        System.setProperty(
                "nats.bootstrap-servers",
                String.format(
                        "nats://%s:%s",
                        jetStreamContainer.getHost(),
                        jetStreamContainer.getMappedPort(4222)));
    }

    private final Connection natsConnection;

    @PostConstruct
    @SneakyThrows
    public void configureJetStreamServer() {
        JetStreamManagement jsm = natsConnection.jetStreamManagement();
        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .name("default-stream")
                .storageType(StorageType.Memory)
                .subjects("basic.example")
                .build();

        jsm.addStream(streamConfig);
    }
}
