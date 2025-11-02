package com.ecommerce.shared.events.impl;

import com.ecommerce.shared.events.BaseEvent;
import com.ecommerce.shared.events.EventPublisher;
import com.ecommerce.shared.events.config.EventsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka implementation of EventPublisher.
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventsProperties eventsProperties;

    @Override
    public <T extends BaseEvent> void publish(T event) {
        String topic = getTopicForEvent(event);
        publish(topic, event, event.getAggregateId());
    }

    @Override
    public <T extends BaseEvent> void publish(String topic, T event) {
        publish(topic, event, event.getAggregateId());
    }

    @Override
    public <T extends BaseEvent> void publish(T event, String partitionKey) {
        String topic = getTopicForEvent(event);
        publish(topic, event, partitionKey);
    }

    @Override
    public <T extends BaseEvent> void publish(String topic, T event, String partitionKey) {
        log.debug("Publishing event {} to topic {} with partition key {}", 
                event.getEventType(), topic, partitionKey);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, partitionKey, event);
        
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} to topic {}: {}", 
                        event.getEventType(), topic, ex.getMessage(), ex);
            } else {
                log.debug("Successfully published event {} to topic {} at offset {}", 
                        event.getEventType(), topic, result.getRecordMetadata().offset());
            }
        });
    }

    private <T extends BaseEvent> String getTopicForEvent(T event) {
        String eventType = event.getEventType().toLowerCase();
        String aggregateType = event.getAggregateType().toLowerCase();

        // Map aggregate types to topics
        switch (aggregateType) {
            case "order":
                return eventsProperties.getTopics().getOrderEvents();
            case "cart":
                return eventsProperties.getTopics().getCartEvents();
            case "product":
                return eventsProperties.getTopics().getProductEvents();
            case "user":
                return eventsProperties.getTopics().getUserEvents();
            case "payment":
                return eventsProperties.getTopics().getPaymentEvents();
            default:
                log.warn("Unknown aggregate type {}, using default topic pattern", aggregateType);
                return aggregateType + "-events";
        }
    }
}