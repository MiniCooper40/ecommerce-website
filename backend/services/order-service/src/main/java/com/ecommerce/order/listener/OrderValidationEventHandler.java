package com.ecommerce.order.listener;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.shared.events.domain.CartValidationCompletedEvent;
import com.ecommerce.shared.events.domain.ProductValidationCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event handler for order validation events.
 * Listens to cart and product validation responses and updates order state.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderValidationEventHandler {

    private final OrderRepository orderRepository;

    /**
     * Handle cart validation completion events.
     * Updates the order's cart validation status.
     */
    @KafkaListener(topics = "cart-events", groupId = "order-service-validation")
    @Transactional
    public void handleCartValidationCompleted(CartValidationCompletedEvent event, Acknowledgment ack) {
        try {
            log.info("Received CartValidationCompletedEvent for orderId: {}, valid: {}", 
                    event.getOrderId(), event.getIsValid());

            // Use the orderId field to find the correct order
            Long orderId = Long.parseLong(event.getOrderId());
            
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setCartValidated(event.getIsValid());
                        
                if (!event.getIsValid()) {
                    log.warn("Cart validation failed for order: {}. Errors: {}", 
                            order.getId(), event.getValidationErrors());
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setValidationCompletedAt(LocalDateTime.now());
                } else {
                    log.info("Cart validation succeeded for order: {}", order.getId());
                    checkAndCompleteValidation(order);
                }
                        
                orderRepository.save(order);
            });

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process CartValidationCompletedEvent for orderId: {}", 
                    event.getOrderId(), e);
            // Don't acknowledge - message will be retried
        }
    }

    /**
     * Handle product validation completion events.
     * Updates the order's stock validation status.
     */
    @KafkaListener(topics = "product-events", groupId = "order-service-validation")
    @Transactional
    public void handleProductValidationCompleted(ProductValidationCompletedEvent event, Acknowledgment ack) {
        try {
            log.info("Received ProductValidationCompletedEvent for requestId: {}, valid: {}", 
                    event.getAggregateId(), event.getIsValid());

            // The requestId should be the orderId
            Long orderId = Long.parseLong(event.getAggregateId());
            
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStockValidated(event.getIsValid());
                
                if (!event.getIsValid()) {
                    log.warn("Stock validation failed for order: {}. Invalid products: {}, Unavailable: {}", 
                            order.getId(), event.getInvalidProducts(), event.getUnavailableProducts());
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setValidationCompletedAt(LocalDateTime.now());
                } else {
                    log.info("Stock validation succeeded for order: {}", order.getId());
                    checkAndCompleteValidation(order);
                }
                
                orderRepository.save(order);
            });

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process ProductValidationCompletedEvent for requestId: {}", 
                    event.getAggregateId(), e);
            // Don't acknowledge - message will be retried
        }
    }

    /**
     * Check if all validations are complete and update order status accordingly.
     */
    private void checkAndCompleteValidation(Order order) {
        if (Boolean.TRUE.equals(order.getCartValidated()) && 
            Boolean.TRUE.equals(order.getStockValidated())) {
            
            log.info("All validations completed successfully for order: {}", order.getId());
            order.setStatus(OrderStatus.CONFIRMED);
            order.setValidationCompletedAt(LocalDateTime.now());
        } else {
            log.debug("Waiting for more validations. Order: {}, cartValidated: {}, stockValidated: {}", 
                    order.getId(), order.getCartValidated(), order.getStockValidated());
        }
    }
}
