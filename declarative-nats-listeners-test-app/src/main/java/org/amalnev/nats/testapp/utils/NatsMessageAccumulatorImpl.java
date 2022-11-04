package org.amalnev.nats.testapp.utils;

import lombok.SneakyThrows;
import org.amalnev.nats.testapp.model.NatsMessageDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NatsMessageAccumulatorImpl implements NatsMessageAccumulator<NatsMessageDto> {
    private final List<NatsMessageDto> accumulatedMessages = new ArrayList<>();

    @Override
    public synchronized void addMessage(NatsMessageDto messagePayload) {
        accumulatedMessages.add(messagePayload);
        notifyAll();
    }

    @Override
    @SneakyThrows
    public synchronized List<NatsMessageDto> waitForMessagesToArrive(int numberOfMessages, int timeoutMs) {
        long elapsedMs = 0;
        while (accumulatedMessages.size() < numberOfMessages && elapsedMs < timeoutMs) {
            long waitStartTimestamp = System.currentTimeMillis();
            wait(timeoutMs);
            long waitEndTimestamp = System.currentTimeMillis();
            elapsedMs += waitEndTimestamp - waitStartTimestamp;
        }

        if (accumulatedMessages.size() < numberOfMessages) {
            throw new IllegalStateException(
                    String.format("Failed to accumulate %s messages in %s ms", numberOfMessages, timeoutMs));
        }

        return accumulatedMessages.stream()
                .map(NatsMessageDto::clone)
                .collect(Collectors.toList());
    }

    @Override
    public NatsMessageDto waitForMessageToArrive(int timeoutMs) {
        return waitForMessagesToArrive(1, timeoutMs).get(0);
    }

    @Override
    public synchronized void resetAccumulatedMessages() {
        accumulatedMessages.clear();
    }

    @Override
    public synchronized int getNumberOfAccumulatedMessages() {
        return accumulatedMessages.size();
    }
}
