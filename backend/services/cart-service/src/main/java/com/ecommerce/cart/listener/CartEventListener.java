package com.ecommerce.cart.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.cart.dto.ProductDto;
import com.ecommerce.cart.entity.CartItemView;
import com.ecommerce.cart.repository.CartItemViewRepository;
import com.ecommerce.cart.service.ProductCacheService;
import com.ecommerce.shared.events.domain.CartItemAddedEvent;
import com.ecommerce.shared.events.domain.CartItemRemovedEvent;
import com.ecommerce.shared.events.domain.CartItemUpdatedEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Event listener for cart item events.
 * Updates the denormalized CartItemView based on cart events.
 */
@Component
@Slf4j
@KafkaListener(topics = "${ecommerce.events.topics.cart-events}", groupId = "cart-service")
public class CartEventListener {

    @Autowired
    private CartItemViewRepository cartItemViewRepository;

    @Autowired
    private ProductCacheService productCacheService;

    /**
     * Handle CartItemAddedEvent - fetch product details and create view entry.
     */
    @KafkaHandler
    @Transactional
    public void handleCartItemAdded(CartItemAddedEvent event) {
        log.info("Handling CartItemAddedEvent: {}", event.getCartItemId());

        try {
            // Fetch product details from cache or catalog service
            ProductDto product = productCacheService.getProduct(event.getProductId());

            if (product == null) {
                log.error("Product not found for productId: {}", event.getProductId());
                return;
            }

            // Create denormalized view entry
            CartItemView view = CartItemView.builder()
                    .cartItemId(event.getCartItemId())
                    .cartId(event.getCartId())
                    .userId(event.getUserId())
                    .productId(event.getProductId())
                    .productName(product.getName())
                    .productDescription(product.getDescription())
                    .productPrice(product.getPrice())
                    .productImageUrl(product.getImageUrl())
                    .productCategory(product.getCategory())
                    .productActive(product.getActive())
                    .quantity(event.getQuantity())
                    .build();

            cartItemViewRepository.save(view);
            log.info("Created CartItemView for cartItemId: {}", event.getCartItemId());

        } catch (Exception e) {
            log.error("Error handling CartItemAddedEvent: {}", event.getCartItemId(), e);
            throw e;
        }
    }

    /**
     * Handle CartItemUpdatedEvent - update quantity in view.
     */
    @KafkaHandler
    @Transactional
    public void handleCartItemUpdated(CartItemUpdatedEvent event) {
        log.info("Handling CartItemUpdatedEvent: {}", event.getCartItemId());

        try {
            CartItemView view = cartItemViewRepository.findByCartItemId(event.getCartItemId());

            if (view != null) {
                view.setQuantity(event.getQuantity());
                cartItemViewRepository.save(view);
                log.info("Updated CartItemView quantity for cartItemId: {}", event.getCartItemId());
            } else {
                log.warn("CartItemView not found for cartItemId: {}", event.getCartItemId());
                // Recreate the view if it doesn't exist
                handleCartItemAdded(CartItemAddedEvent.builder()
                        .cartItemId(event.getCartItemId())
                        .cartId(event.getCartId())
                        .userId(event.getUserId())
                        .productId(event.getProductId())
                        .quantity(event.getQuantity())
                        .source(event.getSource())
                        .build());
            }

        } catch (Exception e) {
            log.error("Error handling CartItemUpdatedEvent: {}", event.getCartItemId(), e);
            throw e;
        }
    }

    /**
     * Handle CartItemRemovedEvent - delete view entry.
     */
    @KafkaHandler
    @Transactional
    public void handleCartItemRemoved(CartItemRemovedEvent event) {
        log.info("Handling CartItemRemovedEvent: {}", event.getCartItemId());

        try {
            cartItemViewRepository.deleteByCartItemId(event.getCartItemId());
            log.info("Deleted CartItemView for cartItemId: {}", event.getCartItemId());

        } catch (Exception e) {
            log.error("Error handling CartItemRemovedEvent: {}", event.getCartItemId(), e);
            throw e;
        }
    }
}
