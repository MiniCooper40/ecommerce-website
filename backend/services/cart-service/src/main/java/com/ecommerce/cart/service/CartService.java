package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.CartItemDto;
import com.ecommerce.cart.dto.CartSummaryDto;

/**
 * Interface for cart service operations
 */
public interface CartService {
    
    /**
     * Get cart summary for a user
     */
    CartSummaryDto getCart(String userId);
    
    /**
     * Add item to cart
     */
    CartItemDto addItemToCart(String userId, CartItemDto cartItemDto);
    
    /**
     * Update cart item
     */
    CartItemDto updateCartItem(String userId, Long itemId, CartItemDto cartItemDto);
    
    /**
     * Update item quantity
     */
    CartItemDto updateItemQuantity(String userId, Long itemId, Integer quantity);
    
    /**
     * Remove item from cart
     */
    void removeItemFromCart(String userId, Long itemId);
    
    /**
     * Clear all items from cart
     */
    void clearCart(String userId);
    
    /**
     * Get total item count in cart
     */
    Integer getCartItemCount(String userId);
    
    /**
     * Update cart items for a specific product
     */
    void updateCartItemsForProduct(Long productId, String productName, java.math.BigDecimal productPrice, String productImageUrl);
}