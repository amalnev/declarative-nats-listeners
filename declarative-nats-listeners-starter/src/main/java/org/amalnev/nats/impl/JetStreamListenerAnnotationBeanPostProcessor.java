package org.amalnev.nats.impl;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.Message;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.amalnev.nats.annotations.JetStreamListener;
import org.amalnev.nats.consumer.DelegatingJetStreamConsumer;
import org.amalnev.nats.consumer.NatsMessageConsumerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        List<JetStreamListenerSpecification> listenerSpecifications = Arrays.stream(bean.getClass().getMethods())
                .filter(method -> Objects.nonNull(method.getAnnotation(JetStreamListener.class)))
                .map(method -> {
                    JetStreamListener listenerAnnotation = method.getAnnotation(JetStreamListener.class);
                    return new JetStreamListenerSpecification()
                            .setBeanName(beanName)
                            .setMethodName(method.getName())
                            .setSubject(listenerAnnotation.subject())
                            .setQueue(listenerAnnotation.queue())
                            .setDeliverPolicy(listenerAnnotation.deliverPolicy())
                            .setConcurrency(listenerAnnotation.concurrency());
                })
                .collect(Collectors.toList());
        if (!listenerSpecifications.isEmpty()) {
            detectedListeners.put(beanName, listenerSpecifications);
        }
    }

    private void createAndRegisterConsumers(Object bean, String beanName) {
        Optional.ofNullable(detectedListeners.get(beanName))
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(listenerSpecification -> {
                    try {
                        Method listenerMethod = bean.getClass().getDeclaredMethod(listenerSpecification.getMethodName(), Message.class);
                        return IntStream.range(0, listenerSpecification.getConcurrency())
                                .mapToObj(i -> new DelegatingJetStreamConsumer(
                                        natsConnection,
                                        jetStream,
                                        listenerSpecification.getSubject(),
                                        listenerSpecification.getQueue(),
                                        listenerSpecification.getDeliverPolicy(),
                                        msg -> invokeListenerMethod(bean, listenerMethod, msg)));
                    } catch (NoSuchMethodException noSuchMethodException) {
                        throw new IllegalStateException(
                                String.format(
                                        "No method with name %s accepting %s argument can be found in bean %s which is of class %s",
                                        listenerSpecification.getMethodName(),
                                        Message.class.getName(),
                                        beanName,
                                        bean.getClass().getName()),
                                noSuchMethodException);
                    }
                })
                .forEach(consumerRegistry::registerConsumer);
    }

    @SneakyThrows
    private void invokeListenerMethod(Object bean, Method listenerMethod, Message argument) {
        try {
            listenerMethod.invoke(bean, argument);
        } catch (InvocationTargetException invocationTargetException) {
            throw invocationTargetException.getCause();
        }
    }
}
