package com.ecommerce.cart.listener;

import java.util.ArrayList;
import java.util.List;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.shared.events.EventPublisher;
import com.ecommerce.shared.events.domain.CartValidationCompletedEvent;
import com.ecommerce.shared.events.domain.CartValidationRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event handler for cart validation requests.
 * Validates cart contents and publishes validation results.
 * 
 * IMPORTANT: Uses CartItem (write model) instead of CartItemView (read model)
 * to avoid eventual consistency issues. Cart validation must check the 
 * authoritative source of truth, not the eventually-consistent view.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = "cart-events", groupId = "cart-service-validation")
public class CartValidationEventHandler {

    private final CartItemRepository cartItemRepository;
    private final EventPublisher eventPublisher;

    /**
     * Handle cart validation requests from order service.
     * Validates that:
     * 1. Cart exists and is not empty
     * 2. All products in cart are still available
     * 3. Quantities match what was requested
     */
    @KafkaHandler
    @Transactional(readOnly = true)
    public void handleCartValidationRequest(CartValidationRequestedEvent event, Acknowledgment ack) {
        log.info("========== CART VALIDATION HANDLER INVOKED ==========");
        log.info("Thread: {}, Event Type: {}", Thread.currentThread().getName(), event.getClass().getSimpleName());
        try {
            log.info("========== CART VALIDATION DEBUG ==========");
            log.info("Received CartValidationRequestedEvent:");
            log.info("  - cartId: {}", event.getAggregateId());
            log.info("  - orderId: {}", event.getOrderId());
            log.info("  - userId: {}", event.getUserId());
            log.info("  - Requested items: {}", event.getItems().size());
            event.getItems().forEach(item -> 
                log.info("    * Product {}: quantity {}", item.getProductId(), item.getQuantity()));

            String userId = event.getUserId();
            List<String> validationErrors = new ArrayList<>();
            
            // Get all cart items for the user from the WRITE MODEL (source of truth)
            // Do NOT use CartItemView here - it's eventually consistent via Kafka events
            List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
            
            log.info("Cart items found in database (write model) for userId {}: {}", userId, cartItems.size());
            cartItems.forEach(item ->
                log.info("    * CartItem: id={}, productId={}, quantity={}", 
                    item.getId(), item.getProductId(), item.getQuantity()));
            
            if (cartItems.isEmpty()) {
                log.warn("Cart is empty for userId: {} - allowing order without cart validation", userId);
                // If cart is empty, we'll allow the order to proceed
                // Product availability will be validated by the catalog service
                log.info("Skipping cart validation - no cart items exist. Product validation will be handled by catalog service.");
            } else {
                // Check each item in the validation request against cart
                for (CartValidationRequestedEvent.CartItem requestedItem : event.getItems()) {
                    Long productId = Long.parseLong(requestedItem.getProductId());
                    Integer requestedQty = requestedItem.getQuantity();
                    
                    log.info("Validating product {}, requested quantity: {}", productId, requestedQty);
                    
                    // Find matching cart item
                    CartItem cartItem = cartItems.stream()
                            .filter(item -> item.getProductId().equals(productId))
                            .findFirst()
                            .orElse(null);
                    
                    if (cartItem == null) {
                        String error = "Product " + productId + " not found in cart";
                        log.error("VALIDATION ERROR: {}", error);
                        validationErrors.add(error);
                    } else if (!cartItem.getQuantity().equals(requestedQty)) {
                        String error = "Quantity mismatch for product " + productId + 
                                ". Expected: " + requestedQty + 
                                ", Found: " + cartItem.getQuantity();
                        log.error("VALIDATION ERROR: {}", error);
                        validationErrors.add(error);
                    } else {
                        log.info("Product {} validation PASSED (qty match: {})", productId, requestedQty);
                    }
                }
            }
            
            boolean isValid = validationErrors.isEmpty();
            log.info("Cart validation result: isValid={}, errors={}", isValid, validationErrors);
            log.info("==========================================");
            
            // Publish validation result
            CartValidationCompletedEvent response = CartValidationCompletedEvent.builder()
                    .cartId(event.getAggregateId())
                    .orderId(event.getOrderId())
                    .userId(userId)
                    .isValid(isValid)
                    .validationErrors(validationErrors)
                    .requestingService(event.getRequestingService())
                    .source("cart-service")
                    .correlationId(event.getCorrelationId())
                    .build();
            
            eventPublisher.publish(response);
            
            log.info("Published CartValidationCompletedEvent for cartId: {}, valid: {}", 
                    event.getAggregateId(), isValid);
            if (!isValid) {
                log.warn("Cart validation failed. Errors: {}", validationErrors);
            }

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process CartValidationRequestedEvent for cartId: {}", 
                    event.getAggregateId(), e);
            // Don't acknowledge - message will be retried
        }
    }

    /**
     * Default handler for unknown event types on cart-events topic with validation group.
     * This prevents errors when CartValidationCompletedEvent (which we publish) or other 
     * events arrive on this topic. We only care about CartValidationRequestedEvent.
     */
    @KafkaHandler(isDefault = true)
    public void handleUnknownEvent(Object event, Acknowledgment ack) {
        log.info("========== DEFAULT HANDLER INVOKED (cart-service-validation group) ==========");
        log.info("Received event on cart-events topic (validation group) that is not a CartValidationRequestedEvent: {}", 
                event.getClass().getSimpleName());
        log.info("Event details: {}", event);
        // Acknowledge and ignore - this is expected for CartValidationCompletedEvent and other events
        ack.acknowledge();
        log.info("========== DEFAULT HANDLER COMPLETED ==========");
    }
}
