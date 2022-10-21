package org.amalnev.nats.impl;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.amalnev.nats.consumer.NatsMessageConsumerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class JetStreamListenerAnnotationBeanPostProcessor implements BeanPostProcessor {

    private final Connection natsConnection;
    private final JetStream jetStream;
    private final NatsMessageConsumerRegistry consumerRegistry;
    private final Map<String, Collection<JetStreamListenerSpecification>> detectedListeners = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        detectJetStreamListenerMethods(bean, beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        createAndRegisterConsumers(bean, beanName);
        return bean;
    }

    @Data
    @Accessors(chain = true)
    private static class JetStreamListenerSpecification {
        private String beanName;
        private String methodName;
        private String subject;
        private String queue;
        private String deliverPolicy;
        private int concurrency;
    }

    private void detectJetStreamListenerMethods(Object bean, String beanName) {

    }

    private void createAndRegisterConsumers(Object bean, String beanName) {

    }
}
