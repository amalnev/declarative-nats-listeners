package org.amalnev.nats.impl;

import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.amalnev.nats.annotations.NatsListener;
import org.amalnev.nats.consumer.DelegatingCoreNatsConsumer;
import org.amalnev.nats.consumer.NatsMessageConsumerRegistry;
import org.amalnev.nats.utils.Reflection;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class NatsListenerAnnotationBeanPostProcessor implements BeanPostProcessor {

    private final Connection natsConnection;
    private final NatsMessageConsumerRegistry consumerRegistry;
    private final Map<String, Collection<NatsListenerSpecification>> detectedListeners = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        detectNatsListenerMethods(bean, beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        createAndRegisterConsumers(bean, beanName);
        return bean;
    }

    @Data
    @Accessors(chain = true)
    private static class NatsListenerSpecification {
        private String beanName;
        private String methodName;
        private String subject;
        private String queue;
        private int concurrency;
    }

    private void detectNatsListenerMethods(Object bean, String beanName) {
        List<NatsListenerSpecification> listenerSpecifications = Arrays.stream(bean.getClass().getMethods())
                .filter(method -> Objects.nonNull(method.getAnnotation(NatsListener.class)))
                .map(method -> {
                    NatsListener listenerAnnotation = method.getAnnotation(NatsListener.class);
                    return new NatsListenerSpecification()
                            .setBeanName(beanName)
                            .setMethodName(method.getName())
                            .setSubject(listenerAnnotation.subject())
                            .setQueue(listenerAnnotation.queue())
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
                                .mapToObj(i -> new DelegatingCoreNatsConsumer(
                                        natsConnection,
                                        listenerSpecification.getSubject(),
                                        listenerSpecification.getQueue(),
                                        msg -> Reflection.invokeListenerMethod(bean, listenerMethod, msg)));
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
}
