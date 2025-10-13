package com.ecommerce.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.cart.service.CartService;

import jakarta.validation.Valid;
import main.java.com.ecommerce.cart.dto.CartItemDto;
import main.java.com.ecommerce.cart.dto.CartSummaryDto;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartSummaryDto> getCart(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemDto> addItemToCart(
            @Valid @RequestBody CartItemDto cartItemDto, 
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cartService.addItemToCart(userId, cartItemDto));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemDto> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemDto cartItemDto,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, itemId, cartItemDto));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") String userId) {
        cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{itemId}/quantity")
    public ResponseEntity<CartItemDto> updateItemQuantity(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, itemId, quantity));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cartService.getCartItemCount(userId));
    }
}