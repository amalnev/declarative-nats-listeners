package org.amalnev.nats.consumer;

public interface NatsMessageConsumerRegistry {
    void registerConsumer(NatsMessageConsumer consumer);
}
