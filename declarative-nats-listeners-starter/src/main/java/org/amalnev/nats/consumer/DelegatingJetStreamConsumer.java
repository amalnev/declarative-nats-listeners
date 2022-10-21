package org.amalnev.nats.consumer;

import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import lombok.SneakyThrows;

import java.util.function.Consumer;

public class DelegatingJetStreamConsumer implements NatsMessageConsumer {
    private final Consumer<Message> delegate;
    private final JetStreamSubscription subscription;

    @SneakyThrows
    public DelegatingJetStreamConsumer(Connection natsConnection,
                                       JetStream jetStream,
                                       String subject,
                                       String queue,
                                       String deliverPolicy,
                                       Consumer<Message> delegate) {
        this.delegate = delegate;
        PushSubscribeOptions subscribeOptions = ConsumerConfiguration.builder()
                .durable(queue)
                .deliverGroup(queue)
                .deliverPolicy(DeliverPolicy.valueOf(deliverPolicy))
                .buildPushSubscribeOptions();
        subscription = jetStream.subscribe(
                subject,
                queue,
                natsConnection.createDispatcher(),
                this::accept,
                false,
                subscribeOptions);
    }

    @Override
    public void accept(Message message) {
        try {
            delegate.accept(message);
            message.ack();
        } catch (Exception ex) {
            message.nak();
        }
    }

    @Override
    public void stop() {
        subscription.unsubscribe();
    }
}
