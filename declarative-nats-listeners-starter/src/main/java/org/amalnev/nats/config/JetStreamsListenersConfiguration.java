package org.amalnev.nats.config;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import lombok.SneakyThrows;
import org.amalnev.nats.consumer.NatsMessageConsumerRegistry;
import org.amalnev.nats.consumer.NatsMessageConsumerRegistryImpl;
import org.amalnev.nats.impl.JetStreamListenerAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class JetStreamsListenersConfiguration {

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(JetStream.class)
    public JetStream jetStream(Connection natsConnection) {
        return natsConnection.jetStream();
    }

    @Bean
    @ConditionalOnMissingBean(NatsMessageConsumerRegistry.class)
    public NatsMessageConsumerRegistry natsMessageConsumerRegistry() {
        return new NatsMessageConsumerRegistryImpl();
    }

    @Bean
    public BeanPostProcessor jetStreamListenerAnnotationBeanPostProcessor(Connection natsConnection,
                                                                          JetStream jetStream,
                                                                          NatsMessageConsumerRegistry consumerRegistry) {
        return new JetStreamListenerAnnotationBeanPostProcessor(
                natsConnection,
                jetStream,
                consumerRegistry);
    }
}
