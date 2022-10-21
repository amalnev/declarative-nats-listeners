package org.amalnev.nats.consumer;

import io.nats.client.Message;

import java.util.function.Consumer;

public interface NatsMessageConsumer extends Consumer<Message> {
    void stop();
}
