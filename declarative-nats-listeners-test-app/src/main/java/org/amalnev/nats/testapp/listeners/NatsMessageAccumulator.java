package org.amalnev.nats.testapp.listeners;

import java.util.List;

public interface NatsMessageAccumulator<T> {

    void addMessage(T messagePayload);

    List<T> waitForMessagesToArrive(int numberOfMessages, int timeoutMs);

    T waitForMessageToArrive(int timeoutMs);
}
