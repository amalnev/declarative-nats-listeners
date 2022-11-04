package org.amalnev.nats.utils;

import io.nats.client.Message;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@UtilityClass
public class Reflection {

    @SneakyThrows
    public void invokeListenerMethod(Object bean, Method listenerMethod, Message argument) {
        try {
            listenerMethod.invoke(bean, argument);
        } catch (InvocationTargetException invocationTargetException) {
            throw invocationTargetException.getCause();
        }
    }
}
