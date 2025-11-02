package com.ecommerce.cart.service;

import java.math.BigDecimal;
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
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public CartSummaryDto getCart(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new CartSummaryDto(cartItemDtos);
    }

    @Override
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
            cartItem = CartItem.builder()
                    .userId(userId)
                    .productId(cartItemDto.getProductId())
                    .productName(cartItemDto.getProductName())
                    .productPrice(cartItemDto.getProductPrice())
                    .productImageUrl(cartItemDto.getProductImageUrl())
                    .quantity(cartItemDto.getQuantity())
                    .build();
        }

        cartItem = cartItemRepository.save(cartItem);
        return convertToDto(cartItem);
    }

    @Override
    public CartItemDto updateCartItem(String userId, Long itemId, CartItemDto cartItemDto) {
        CartItem cartItem = cartItemRepository.findByUserIdAndId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartItem.setQuantity(cartItemDto.getQuantity());
        cartItem = cartItemRepository.save(cartItem);
        
        return convertToDto(cartItem);
    }

    @Override
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

    @Override
    public void removeItemFromCart(String userId, Long itemId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    public Integer getCartItemCount(String userId) {
        Integer count = cartItemRepository.sumQuantityByUserId(userId);
        return count != null ? count : 0;
    }

    @Override
    public void updateCartItemsForProduct(Long productId, String productName, BigDecimal productPrice, String productImageUrl) {
        List<CartItem> cartItems = cartItemRepository.findByProductId(productId);
        
        for (CartItem cartItem : cartItems) {
            cartItem.setProductName(productName);
            cartItem.setProductPrice(productPrice);
            cartItem.setProductImageUrl(productImageUrl);
        }
        
        cartItemRepository.saveAll(cartItems);
    }

    private CartItemDto convertToDto(CartItem cartItem) {
        return CartItemDto.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .productPrice(cartItem.getProductPrice())
                .productImageUrl(cartItem.getProductImageUrl())
                .quantity(cartItem.getQuantity())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}