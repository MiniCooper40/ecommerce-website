package com.ecommerce.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.cart.dto.CartItemDto;
import com.ecommerce.cart.dto.CartSummaryDto;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.security.annotation.CurrentUserEmail;
import com.ecommerce.security.annotation.CurrentUserId;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartSummaryDto> getCart(@CurrentUserId String userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemDto> addItemToCart(
            @Valid @RequestBody CartItemDto cartItemDto, 
            @CurrentUserId String userId) {
        return ResponseEntity.ok(cartService.addItemToCart(userId, cartItemDto));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemDto> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemDto cartItemDto,
            @CurrentUserId String userId) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, itemId, cartItemDto));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long itemId,
            @CurrentUserId String userId) {
        cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{itemId}/quantity")
    public ResponseEntity<CartItemDto> updateItemQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            @CurrentUserId String userId) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, itemId, quantity));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@CurrentUserId String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(@CurrentUserId String userId) {
        return ResponseEntity.ok(cartService.getCartItemCount(userId));
    }

    @GetMapping("/user-info")
    public Object getUserInfo(
            @CurrentUserId String userId, 
            @CurrentUserEmail String email,
            @AuthenticationPrincipal Jwt jwt) {
        // Endpoint to see JWT claims and user info
        return java.util.Map.of(
            "userId", userId, // Clean annotation
            "email", email,   // Clean annotation
            "firstName", jwt.getClaimAsString("firstName"),
            "lastName", jwt.getClaimAsString("lastName"),
            "roles", jwt.getClaimAsStringList("roles")
        );
    }
}