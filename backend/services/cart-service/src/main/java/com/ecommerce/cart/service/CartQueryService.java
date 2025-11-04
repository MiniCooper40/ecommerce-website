package com.ecommerce.cart.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.cart.dto.CartItemDto;
import com.ecommerce.cart.dto.CartSummaryDto;
import com.ecommerce.cart.entity.CartItemView;
import com.ecommerce.cart.repository.CartItemViewRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Query service for read operations on cart items.
 * Follows CQRS pattern - handles queries from the denormalized view.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class CartQueryService {

    @Autowired
    private CartItemViewRepository cartItemViewRepository;

    /**
     * Get the complete cart for a user.
     */
    public CartSummaryDto getCart(String userId) {
        log.info("Getting cart for user: {}", userId);

        List<CartItemView> cartItems = cartItemViewRepository.findByUserId(userId);
        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new CartSummaryDto(cartItemDtos);
    }

    /**
     * Get the count of items in the user's cart.
     */
    public Integer getCartItemCount(String userId) {
        log.info("Getting cart item count for user: {}", userId);

        Integer count = cartItemViewRepository.sumQuantityByUserId(userId);
        return count != null ? count : 0;
    }

    private CartItemDto convertToDto(CartItemView view) {
        return CartItemDto.builder()
                .id(view.getCartItemId())
                .productId(view.getProductId())
                .productName(view.getProductName())
                .productPrice(view.getProductPrice())
                .productImageUrl(view.getProductImageUrl())
                .quantity(view.getQuantity())
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .build();
    }
}
