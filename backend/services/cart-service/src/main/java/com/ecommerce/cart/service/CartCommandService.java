package com.ecommerce.cart.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.shared.events.EventPublisher;
import com.ecommerce.shared.events.domain.CartItemAddedEvent;
import com.ecommerce.shared.events.domain.CartItemRemovedEvent;
import com.ecommerce.shared.events.domain.CartItemUpdatedEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Command service for write operations on cart items.
 * Follows CQRS pattern - handles commands and publishes events.
 */
@Service
@Transactional
@Slf4j
public class CartCommandService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private EventPublisher eventPublisher;

    /**
     * Add a new item to the cart or update quantity if it already exists.
     */
    public Long addItemToCart(String userId, Long productId, Integer quantity) {
        log.info("Adding item to cart - userId: {}, productId: {}, quantity: {}", 
                 userId, productId, quantity);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByUserIdAndProductId(userId, productId);

        CartItem cartItem;
        boolean isUpdate = false;

        if (existingItem.isPresent()) {
            // Update quantity if item already exists
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            isUpdate = true;
            log.info("Updating existing cart item - id: {}, new quantity: {}", 
                     cartItem.getId(), cartItem.getQuantity());
        } else {
            // Create new cart item
            String cartId = UUID.randomUUID().toString();
            cartItem = CartItem.builder()
                    .cartId(cartId)
                    .userId(userId)
                    .productId(productId)
                    .quantity(quantity)
                    .build();
            log.info("Creating new cart item with cartId: {}", cartId);
        }

        cartItem = cartItemRepository.save(cartItem);

        // Publish event
        if (isUpdate) {
            publishCartItemUpdatedEvent(cartItem);
        } else {
            publishCartItemAddedEvent(cartItem);
        }

        return cartItem.getId();
    }

    /**
     * Update the quantity of a cart item.
     */
    public void updateItemQuantity(String userId, Long itemId, Integer quantity) {
        log.info("Updating cart item quantity - userId: {}, itemId: {}, quantity: {}", 
                 userId, itemId, quantity);

        CartItem cartItem = cartItemRepository.findByUserIdAndId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            removeItemFromCart(userId, itemId);
            return;
        }

        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);

        publishCartItemUpdatedEvent(cartItem);
    }

    /**
     * Remove an item from the cart.
     */
    public void removeItemFromCart(String userId, Long itemId) {
        log.info("Removing cart item - userId: {}, itemId: {}", userId, itemId);

        CartItem cartItem = cartItemRepository.findByUserIdAndId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartItemRepository.delete(cartItem);

        publishCartItemRemovedEvent(cartItem);
    }

    /**
     * Clear all items from user's cart.
     */
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);

        var cartItems = cartItemRepository.findByUserId(userId);
        
        cartItemRepository.deleteByUserId(userId);

        // Publish removed events for all items
        cartItems.forEach(this::publishCartItemRemovedEvent);
    }

    private void publishCartItemAddedEvent(CartItem cartItem) {
        CartItemAddedEvent event = CartItemAddedEvent.builder()
                .cartItemId(cartItem.getId())
                .cartId(cartItem.getCartId())
                .userId(cartItem.getUserId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .source("cart-service")
                .build();

        eventPublisher.publish(event);
        log.info("Published CartItemAddedEvent for cartItemId: {}", cartItem.getId());
    }

    private void publishCartItemUpdatedEvent(CartItem cartItem) {
        CartItemUpdatedEvent event = CartItemUpdatedEvent.builder()
                .cartItemId(cartItem.getId())
                .cartId(cartItem.getCartId())
                .userId(cartItem.getUserId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .source("cart-service")
                .build();

        eventPublisher.publish(event);
        log.info("Published CartItemUpdatedEvent for cartItemId: {}", cartItem.getId());
    }

    private void publishCartItemRemovedEvent(CartItem cartItem) {
        CartItemRemovedEvent event = CartItemRemovedEvent.builder()
                .cartItemId(cartItem.getId())
                .cartId(cartItem.getCartId())
                .userId(cartItem.getUserId())
                .productId(cartItem.getProductId())
                .source("cart-service")
                .build();

        eventPublisher.publish(event);
        log.info("Published CartItemRemovedEvent for cartItemId: {}", cartItem.getId());
    }
}
