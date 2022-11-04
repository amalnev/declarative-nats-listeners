package org.amalnev.nats.testapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.impl.NatsMessage;
import lombok.SneakyThrows;
import org.amalnev.nats.testapp.model.NatsMessageDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

@SpringBootTest
public class BasicExamplesTest {

    @Autowired
    private JetStream jetStream;

    @Autowired
    private Connection natsConnection;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    public void runBasicJetStreamExampleTest() {
        NatsMessageDto message = new NatsMessageDto()
                .setStringField("a string value")
                .setIntegerField(100);
        String subject = "basic.jetstream.example";

        jetStream.publish(
                new NatsMessage.Builder()
                        .subject(subject)
                        .data(objectMapper.writeValueAsString(message), StandardCharsets.UTF_8)
                        .build());

        Thread.sleep(5_000);
    }

    @Test
    @SneakyThrows
    public void runBasicCoreNatsExampleTest() {
        NatsMessageDto message = new NatsMessageDto()
                .setStringField("a string value")
                .setIntegerField(101);
        String subject = "basic.core.example";

        natsConnection.publish(
                new NatsMessage.Builder()
                        .subject(subject)
                        .data(objectMapper.writeValueAsString(message), StandardCharsets.UTF_8)
                        .build());

        Thread.sleep(5_000);
    }
}
