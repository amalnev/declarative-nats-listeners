package org.amalnev.nats.consumer;

import lombok.SneakyThrows;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NatsMessageConsumerRegistryImpl implements NatsMessageConsumerRegistry {
    private final Collection<NatsMessageConsumer> registeredConsumers = new ArrayList<>();

    @Override
    public void registerConsumer(NatsMessageConsumer consumer) {
        registeredConsumers.add(consumer);
    }

    @SneakyThrows
    @PreDestroy
    public void stopConsumers() {
        List<Throwable> consumerStopErrors = registeredConsumers.stream()
                .map(consumer -> {
                    try {
                        consumer.stop();
                        return null;
                    } catch (Exception ex) {
                        return ex;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!consumerStopErrors.isEmpty()) {
            if (consumerStopErrors.size() == 1) {
                throw consumerStopErrors.get(0);
            } else {
                IllegalStateException ex = new IllegalStateException("Errors encountered while stopping NATS consumers");
                consumerStopErrors.forEach(ex::addSuppressed);
                throw ex;
            }
        }
    }
}
