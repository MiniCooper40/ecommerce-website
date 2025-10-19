package com.ecommerce.cart.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.cart.dto.CartItemDto;
import com.ecommerce.cart.dto.CartSummaryDto;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    public CartSummaryDto getCart(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new CartSummaryDto(cartItemDtos);
    }

    public CartItemDto addItemToCart(String userId, CartItemDto cartItemDto) {
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByUserIdAndProductId(userId, cartItemDto.getProductId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update quantity if item already exists
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemDto.getQuantity());
        } else {
            // Create new cart item
            cartItem = new CartItem(
                    userId,
                    cartItemDto.getProductId(),
                    cartItemDto.getProductName(),
                    cartItemDto.getProductPrice(),
                    cartItemDto.getProductImageUrl(),
                    cartItemDto.getQuantity()
            );
        }

        cartItem = cartItemRepository.save(cartItem);
        return convertToDto(cartItem);
    }

    public CartItemDto updateCartItem(String userId, Long itemId, CartItemDto cartItemDto) {
        CartItem cartItem = cartItemRepository.findByUserIdAndId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartItem.setQuantity(cartItemDto.getQuantity());
        cartItem = cartItemRepository.save(cartItem);
        
        return convertToDto(cartItem);
    }

    public CartItemDto updateItemQuantity(String userId, Long itemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findByUserIdAndId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        
        return convertToDto(cartItem);
    }

    public void removeItemFromCart(String userId, Long itemId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        cartItemRepository.delete(cartItem);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public Integer getCartItemCount(String userId) {
        Integer count = cartItemRepository.sumQuantityByUserId(userId);
        return count != null ? count : 0;
    }

    private CartItemDto convertToDto(CartItem cartItem) {
        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProductId());
        dto.setProductName(cartItem.getProductName());
        dto.setProductPrice(cartItem.getProductPrice());
        dto.setProductImageUrl(cartItem.getProductImageUrl());
        dto.setQuantity(cartItem.getQuantity());
        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());
        return dto;
    }
}