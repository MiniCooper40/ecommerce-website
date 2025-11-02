package com.ecommerce.shared.events.annotation;

import org.springframework.kafka.annotation.KafkaListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for event handlers that consume domain events.
 * Simplifies the setup of Kafka listeners with sensible defaults.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@KafkaListener
public @interface EventListener {

    /**
     * The topics to listen to.
     */
    String[] topics() default {};

    /**
     * The consumer group ID.
     */
    String groupId() default "#{T(java.util.UUID).randomUUID().toString()}";

    /**
     * Whether this listener should automatically acknowledge messages.
     */
    boolean autoAck() default false;

    /**
     * Error handler bean name.
     */
    String errorHandler() default "";

    /**
     * Container factory bean name.
     */
    String containerFactory() default "kafkaListenerContainerFactory";
}