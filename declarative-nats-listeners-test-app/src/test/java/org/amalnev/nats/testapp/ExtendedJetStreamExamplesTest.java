package org.amalnev.nats.testapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.JetStream;
import io.nats.client.Message;
import io.nats.client.impl.NatsMessage;
import lombok.SneakyThrows;
import org.amalnev.nats.testapp.model.NatsMessageDto;
import org.amalnev.nats.testapp.utils.NatsMessageAccumulator;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

@SpringBootTest
public class ExtendedJetStreamExamplesTest {

    @Autowired
    private JetStream jetStream;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NatsMessageAccumulator<NatsMessageDto> messageAccumulator;

    private EasyRandom random;

    @BeforeEach
    private void configureTests() {
        random = new EasyRandom();
        messageAccumulator.resetAccumulatedMessages();
    }

    @Test
    @SneakyThrows
    public void runExtendedJetStreamExamplesTest() {
        IntStream.range(0, 100)
                .mapToObj(i -> random.nextObject(NatsMessageDto.class))
                .map(it -> new NatsMessage.Builder()
                        .subject("extended.jetstream.example.subject.1")
                        .data(writeValueAsString(it), StandardCharsets.UTF_8)
                        .build())
                .forEach(this::publish);

        messageAccumulator.waitForMessagesToArrive(200, 20_000);
        Thread.sleep(1_000);
        Assertions.assertEquals(200, messageAccumulator.getNumberOfAccumulatedMessages());
    }

    @SneakyThrows
    private String writeValueAsString(NatsMessageDto messageDto) {
        return objectMapper.writeValueAsString(messageDto);
    }

    @SneakyThrows
    private void publish(Message message) {
        jetStream.publish(message);
    }
}
