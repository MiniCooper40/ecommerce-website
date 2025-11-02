package com.ecommerce.shared.events;

/**
 * Interface for event publishers.
 * Provides a simple abstraction for publishing domain events.
 */
public interface EventPublisher {

    /**
     * Publishes an event to the appropriate topic.
     *
     * @param event the event to publish
     * @param <T> the event type
     */
    <T extends BaseEvent> void publish(T event);

    /**
     * Publishes an event to a specific topic.
     *
     * @param topic the topic to publish to
     * @param event the event to publish
     * @param <T> the event type
     */
    <T extends BaseEvent> void publish(String topic, T event);

    /**
     * Publishes an event with a specific partition key.
     *
     * @param event the event to publish
     * @param partitionKey the partition key for message ordering
     * @param <T> the event type
     */
    <T extends BaseEvent> void publish(T event, String partitionKey);

    /**
     * Publishes an event to a specific topic with a partition key.
     *
     * @param topic the topic to publish to
     * @param event the event to publish
     * @param partitionKey the partition key for message ordering
     * @param <T> the event type
     */
    <T extends BaseEvent> void publish(String topic, T event, String partitionKey);
}