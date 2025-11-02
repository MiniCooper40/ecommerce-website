package com.ecommerce.shared.events;

/**
 * Interface for event handlers.
 * Implement this interface to handle specific event types.
 *
 * @param <T> the event type to handle
 */
@FunctionalInterface
public interface EventHandler<T extends BaseEvent> {

    /**
     * Handles the given event.
     *
     * @param event the event to handle
     */
    void handle(T event);
}