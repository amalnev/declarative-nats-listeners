package org.amalnev.nats.testapp.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.amalnev.nats.annotations.JetStreamListener;
import org.amalnev.nats.testapp.model.NatsMessageDto;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicExample {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @JetStreamListener(subject = "basic.example")
    public void basicExampleJetStreamListener(Message natsMessage) {
        String messagePayloadAsString = new String(natsMessage.getData(), StandardCharsets.UTF_8);
        NatsMessageDto messagePayloadAsObject = objectMapper.readValue(
                messagePayloadAsString,
                NatsMessageDto.class);

        log.info(
                "{}",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messagePayloadAsObject));
    }
}
