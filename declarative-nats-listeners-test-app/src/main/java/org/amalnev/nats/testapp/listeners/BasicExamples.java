package org.amalnev.nats.testapp.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.amalnev.nats.annotations.JetStreamListener;
import org.amalnev.nats.annotations.NatsListener;
import org.amalnev.nats.testapp.model.NatsMessageDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicExamples {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @JetStreamListener(subject = "basic.jetstream.example")
    public void basicJetStreamListenerExample(Message natsMessage) {
        parseAndLogMessage(natsMessage);
    }

    @SneakyThrows
    @NatsListener(subject = "basic.core.example")
    public void basicCoreNatsListenerExample(Message natsMessage) {
        parseAndLogMessage(natsMessage);
    }

    @SneakyThrows
    private void parseAndLogMessage(Message natsMessage) {
        NatsMessageDto messagePayloadAsObject = NatsMessageDto.fromNatsMessage(natsMessage, objectMapper);

        log.info(
                "{}",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messagePayloadAsObject));
    }
}
