package com.ecommerce.cart.config;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.ecommerce.cart.service.CartService;
import com.ecommerce.shared.events.domain.ProductUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event listener for product-related events in the cart service.
 * Updates cart items when products are modified.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final CartService cartService;

    /**
     * Handles ProductUpdatedEvent to sync cart items with updated product information.
     */
    @KafkaListener(topics = "product-events", groupId = "cart-service")
    public void handleProductUpdated(
            @Payload ProductUpdatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Received ProductUpdatedEvent for product {} from topic {} (partition: {}, offset: {})", 
                event.getAggregateId(), topic, partition, offset);
        
        try {
            // Update all cart items that contain this product
            cartService.updateCartItemsForProduct(
                    Long.valueOf(event.getAggregateId()),
                    event.getName(),
                    event.getPrice(),
                    event.getImageUrl()
            );
            
            log.info("Successfully updated cart items for product {} (correlation: {})", 
                    event.getAggregateId(), event.getCorrelationId());
            
        } catch (Exception e) {
            log.error("Failed to update cart items for product {} (correlation: {}): {}", 
                    event.getAggregateId(), event.getCorrelationId(), e.getMessage(), e);
            // In a real system, you might want to publish a compensation event
            // or store the failed event for retry
        }
    }
}