package com.ecommerce.catalog.events;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.shared.events.EventPublisher;
import com.ecommerce.shared.events.domain.ProductUpdatedEvent;
import com.ecommerce.shared.events.domain.ProductValidationCompletedEvent;
import com.ecommerce.shared.events.domain.ProductValidationRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event handlers for product-related events in the catalog service.
 * This shows how the catalog service can both publish and consume events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventHandler {

    private final ProductService productService;
    private final EventPublisher eventPublisher;

    /**
     * Handle product validation requests from other services.
     * This could be triggered when an order service needs to validate products.
     */
    @KafkaListener(topics = "product-events", groupId = "catalog-service-validation")
    public void handleProductValidationRequest(ProductValidationRequestedEvent event, Acknowledgment ack) {
        try {
            log.info("Received product validation request for products: {} from service: {}", 
                    event.getProductIds(), event.getRequestingService());

            List<String> validProducts = event.getProductIds().stream()
                    .filter(productId -> {
                        try {
                            Long id = Long.parseLong(productId);
                            return productService.isProductAvailable(id, 1);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid product ID format: {}", productId);
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            List<String> invalidProducts = event.getProductIds().stream()
                    .filter(productId -> !validProducts.contains(productId))
                    .collect(Collectors.toList());

            // Check specific quantities if provided
            List<ProductValidationCompletedEvent.ProductAvailability> unavailableProducts = 
                    Collections.emptyList();
            
            if (event.getRequiredQuantities() != null) {
                unavailableProducts = event.getRequiredQuantities().stream()
                        .filter(pq -> {
                            try {
                                Long id = Long.parseLong(pq.getProductId());
                                return !productService.isProductAvailable(id, pq.getRequiredQuantity());
                            } catch (NumberFormatException e) {
                                return true;
                            }
                        })
                        .map(pq -> ProductValidationCompletedEvent.ProductAvailability.builder()
                                .productId(pq.getProductId())
                                .requestedQuantity(pq.getRequiredQuantity())
                                .availableQuantity(getAvailableStock(pq.getProductId()))
                                .build())
                        .collect(Collectors.toList());
            }

            boolean isValid = invalidProducts.isEmpty() && unavailableProducts.isEmpty();

            ProductValidationCompletedEvent response = ProductValidationCompletedEvent.builder()
                    .requestId(event.getAggregateId())
                    .validProducts(validProducts)
                    .invalidProducts(invalidProducts)
                    .unavailableProducts(unavailableProducts)
                    .isValid(isValid)
                    .requestingService(event.getRequestingService())
                    .source("catalog-service")
                    .correlationId(event.getCorrelationId())
                    .build();

            eventPublisher.publish(response);
            
            log.info("Sent product validation response for request: {} - Valid: {}", 
                    event.getAggregateId(), isValid);

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process product validation request: {}", event.getAggregateId(), e);
            // Don't acknowledge - message will be retried
        }
    }

    /**
     * Example handler that could be used by other services to react to product updates.
     * This is just for demonstration - it would typically be in a different service.
     */
    @KafkaListener(topics = "product-events", groupId = "catalog-service-updates")
    public void handleProductUpdated(ProductUpdatedEvent event, Acknowledgment ack) {
        try {
            log.info("Product updated: {} - {} (Stock: {})", 
                    event.getAggregateId(), event.getName(), event.getStockQuantity());

            // Example: Update local cache, invalidate search indices, etc.
            // In a real scenario, this might be in the order service or cart service
            
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process product update event: {}", event.getAggregateId(), e);
        }
    }

    private Integer getAvailableStock(String productId) {
        try {
            Long id = Long.parseLong(productId);
            var product = productService.getProduct(id);
            return product.getStockQuantity();
        } catch (Exception e) {
            log.warn("Could not get stock for product: {}", productId);
            return 0;
        }
    }
}