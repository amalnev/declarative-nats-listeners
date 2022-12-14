package org.amalnev.nats.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JetStreamListener {

    String subject();

    String queue() default "";

    String deliverPolicy() default "New";

    int concurrency() default 1;
}
