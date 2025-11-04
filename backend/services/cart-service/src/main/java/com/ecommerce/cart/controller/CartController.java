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

import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.CartSummaryDto;
import com.ecommerce.cart.service.CartCommandService;
import com.ecommerce.cart.service.CartQueryService;
import com.ecommerce.security.annotation.CurrentUserEmail;
import com.ecommerce.security.annotation.CurrentUserId;

import jakarta.validation.Valid;

/**
 * Controller for cart operations following CQRS pattern.
 * Separates command (write) and query (read) operations.
 */
@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartCommandService cartCommandService;

    @Autowired
    private CartQueryService cartQueryService;

    // ========== Query Operations (Read) ==========

    @GetMapping
    public ResponseEntity<CartSummaryDto> getCart(@CurrentUserId String userId) {
        return ResponseEntity.ok(cartQueryService.getCart(userId));
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(@CurrentUserId String userId) {
        return ResponseEntity.ok(cartQueryService.getCartItemCount(userId));
    }

    // ========== Command Operations (Write) ==========

    @PostMapping("/items")
    public ResponseEntity<Long> addItemToCart(
            @Valid @RequestBody AddCartItemRequest request, 
            @CurrentUserId String userId) {
        Long cartItemId = cartCommandService.addItemToCart(
                userId, 
                request.getProductId(), 
                request.getQuantity());
        return ResponseEntity.ok(cartItemId);
    }

    @PutMapping("/items/{itemId}/quantity")
    public ResponseEntity<Void> updateItemQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            @CurrentUserId String userId) {
        cartCommandService.updateItemQuantity(userId, itemId, quantity);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long itemId,
            @CurrentUserId String userId) {
        cartCommandService.removeItemFromCart(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@CurrentUserId String userId) {
        cartCommandService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    // ========== Utility Endpoints ==========

    @GetMapping("/user-info")
    public Object getUserInfo(
            @CurrentUserId String userId, 
            @CurrentUserEmail String email,
            @AuthenticationPrincipal Jwt jwt) {
        // Endpoint to see JWT claims and user info
        return java.util.Map.of(
            "userId", userId,
            "email", email,
            "firstName", jwt.getClaimAsString("firstName"),
            "lastName", jwt.getClaimAsString("lastName"),
            "roles", jwt.getClaimAsStringList("roles")
        );
    }
}
