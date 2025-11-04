package com.ecommerce.cart.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.cart.dto.ProductDto;
import com.ecommerce.cart.repository.CartItemViewRepository;
import com.ecommerce.cart.service.ProductCacheService;
import com.ecommerce.shared.events.domain.ProductUpdatedEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Event listener for product events.
 * Updates cart item views when products are updated.
 */
@Component
@Slf4j
@KafkaListener(topics = "${ecommerce.events.topics.product-events}", groupId = "cart-service")
public class ProductEventListener {

    @Autowired
    private CartItemViewRepository cartItemViewRepository;

    @Autowired
    private ProductCacheService productCacheService;

    /**
     * Handle ProductUpdatedEvent - update all cart items with new product details.
     */
    @KafkaHandler
    @Transactional
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.info("Handling ProductUpdatedEvent for productId: {}", event.getAggregateId());

        try {
            Long productId = Long.parseLong(event.getAggregateId());

            // Update cache
            ProductDto product = ProductDto.builder()
                    .id(productId)
                    .name(event.getName())
                    .description(event.getDescription())
                    .price(event.getPrice())
                    .currency(event.getCurrency())
                    .stockQuantity(event.getStockQuantity())
                    .category(event.getCategory())
                    .imageUrl(event.getImageUrl())
                    .active(event.getActive())
                    .build();

            productCacheService.updateProductCache(product);

            // Update all cart item views with this product
            cartItemViewRepository.updateProductDetailsForProduct(
                    productId,
                    event.getName(),
                    event.getDescription(),
                    event.getPrice(),
                    event.getImageUrl(),
                    event.getCategory(),
                    event.getActive()
            );

            log.info("Updated all CartItemViews for productId: {}", productId);

        } catch (Exception e) {
            log.error("Error handling ProductUpdatedEvent: {}", event.getAggregateId(), e);
            throw e;
        }
    }
}
