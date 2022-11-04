package org.amalnev.nats.config;

import io.nats.client.Connection;
import org.amalnev.nats.consumer.NatsMessageConsumerRegistry;
import org.amalnev.nats.consumer.NatsMessageConsumerRegistryImpl;
import org.amalnev.nats.impl.NatsListenerAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class NatsListenersConfiguration {

    @Bean
    @ConditionalOnMissingBean(NatsMessageConsumerRegistry.class)
    public NatsMessageConsumerRegistry natsMessageConsumerRegistry() {
        return new NatsMessageConsumerRegistryImpl();
    }

    @Bean
    public BeanPostProcessor natsListenerAnnotationBeanPostProcessor(Connection natsConnection,
                                                                     NatsMessageConsumerRegistry consumerRegistry) {
        return new NatsListenerAnnotationBeanPostProcessor(
                natsConnection,
                consumerRegistry);
    }
}
