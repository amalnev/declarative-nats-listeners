package org.amalnev.nats.consumer;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class DelegatingCoreNatsConsumer implements NatsMessageConsumer {
    private final Consumer<Message> delegate;
    private final Dispatcher dispatcher;
    private final String subject;

    public DelegatingCoreNatsConsumer(Connection natsConnection,
                                      String subject,
                                      String queue,
                                      Consumer<Message> delegate) {
        this.delegate = delegate;
        this.subject = subject;
        String queueName = Optional.of(queue)
                .filter(it -> !it.isBlank())
                .orElse(UUID.randomUUID().toString());
        this.dispatcher = natsConnection.createDispatcher(this::accept).subscribe(subject, queueName);
    }

    @Override
    public void stop() {
        dispatcher.unsubscribe(subject);
    }

    @Override
    public void accept(Message message) {
        delegate.accept(message);
    }
}
