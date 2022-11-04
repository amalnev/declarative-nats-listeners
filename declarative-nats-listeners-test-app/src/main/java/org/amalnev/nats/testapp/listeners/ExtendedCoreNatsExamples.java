package org.amalnev.nats.testapp.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.amalnev.nats.annotations.NatsListener;
import org.amalnev.nats.testapp.model.NatsMessageDto;
import org.amalnev.nats.testapp.utils.NatsMessageAccumulator;
import org.springframework.stereotype.Service;

/**
 * This example demonstrates the use of 'queue' parameter of @NatsListener
 * annotation. It basically acts as a consumer group id in @KafkaListener annotation
 * provided by Spring Kafka.
 * <p>
 * If 2 listeners belong to the same queue they will receive
 * only one copy of each message published to the corresponding subject.
 * <p>
 * If 2 listeners belong to different queues they will each get their own
 * copy of each message published to the corresponding subject.
 * <p>
 * Queue names are consistent across multiple instances of the same application/microservice.
 * E.g. 2 @NatsListener-s with the same value for the queue parameter located in different
 * JVM processes will still work as a group, i.e. they will both get a single copy of each
 * incoming message, thus facilitating concurrent processing. This allows to scale the
 * processing horizontally.
 * <p>
 * If the queue parameter is not specified in @NatsListener annotation explicitly,
 * the value for it will be generated randomly and uniquely at runtime, resulting in that
 * listener to be independent of all other listeners subscribed to the same subject (it will
 * get its own copy of incoming messages).
 * <p>
 * Concurrency parameter provides more granularity to configure the horizontal scaling of
 * message processing. It basically tells how many concurrent threads to start to process
 * messages from the corresponding subject. In this particular example specifying
 * concurrency=2 for queue1ConcurrentListener() will result in method queue1ConcurrentListener() being
 * called by 2 concurrent threads, while other methods (queue1SerialListener() and independentListener())
 * will each be called by a separate and single thread (since the default value for
 * 'concurrency' parameter of @NatsListener annotation is 1).
 * <p>
 * As a result, in this particular example we will have 3 threads (1 for queue1SerialListener()
 * and 2 for queue1ConcurrentListener()) reading incoming messages from a subject in parallel, and
 * another independent thread (for independentListener()) getting its own copies of incoming
 * messages. If we publish 10 messages to 'extended.core.example.subject.1', then 20
 * instances of messages will be sent to the messageAccumulator.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtendedCoreNatsExamples {

    private final NatsMessageAccumulator<NatsMessageDto> messageAccumulator;
    private final ObjectMapper objectMapper;

    @NatsListener(subject = "extended.core.example.subject.1", queue = "queue1")
    public void queue1SerialListener(Message natsMessage) {
        messageAccumulator.addMessage(parseAndLogMessage(natsMessage));
    }

    @NatsListener(subject = "extended.core.example.subject.1", queue = "queue1", concurrency = 2)
    public void queue1ConcurrentListener(Message natsMessage) {
        messageAccumulator.addMessage(parseAndLogMessage(natsMessage));
    }

    @NatsListener(subject = "extended.core.example.subject.1")
    public void independentListener(Message natsMessage) {
        messageAccumulator.addMessage(parseAndLogMessage(natsMessage));
    }

    @SneakyThrows
    private NatsMessageDto parseAndLogMessage(Message natsMessage) {
        NatsMessageDto messagePayloadAsObject = NatsMessageDto.fromNatsMessage(natsMessage, objectMapper);

        log.info(
                "{}: {}",
                Thread.currentThread().getName(),
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messagePayloadAsObject));
        return messagePayloadAsObject;
    }
}
