package org.amalnev.nats.testapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.JetStream;
import io.nats.client.impl.NatsMessage;
import lombok.SneakyThrows;
import org.amalnev.nats.testapp.model.NatsMessageDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

@SpringBootTest
public class BasicExampleTest {

    @Autowired
    private JetStream jetStream;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    public void runBasicExampleTest() {

        //publish a test message to basic.example subject
        jetStream.publish(
                new NatsMessage.Builder()
                        .subject("basic.example")
                        .data(objectMapper.writeValueAsString(
                                        new NatsMessageDto()
                                                .setStringField("a string value")
                                                .setIntegerField(100)),
                                StandardCharsets.UTF_8)
                        .build());

        //wait for some time for the BasicExample listener to catch our message
        //and write it to console
        Thread.sleep(5_000);
    }
}
