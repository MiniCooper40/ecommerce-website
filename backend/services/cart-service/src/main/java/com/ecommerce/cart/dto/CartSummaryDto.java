package com.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartSummaryDto {
    
    private List<CartItemDto> items;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shipping;
    private BigDecimal total;

    // Custom constructor that calculates totals
    public CartSummaryDto(List<CartItemDto> items) {
        this.items = items;
        this.calculateTotals();
    }

    // Custom setter for items that recalculates totals
    public void setItems(List<CartItemDto> items) {
        this.items = items;
        this.calculateTotals();
    }

    // Utility methods
    private void calculateTotals() {
        if (items == null || items.isEmpty()) {
            this.totalItems = 0;
            this.subtotal = BigDecimal.ZERO;
            this.tax = BigDecimal.ZERO;
            this.shipping = BigDecimal.ZERO;
            this.total = BigDecimal.ZERO;
            return;
        }

        this.totalItems = items.stream()
            .mapToInt(CartItemDto::getQuantity)
            .sum();

        this.subtotal = items.stream()
            .map(CartItemDto::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate tax (8.5% for example)
        this.tax = subtotal.multiply(BigDecimal.valueOf(0.085));

        // Calculate shipping (free over $50, otherwise $5.99)
        this.shipping = subtotal.compareTo(BigDecimal.valueOf(50)) >= 0 
            ? BigDecimal.ZERO 
            : BigDecimal.valueOf(5.99);

        this.total = subtotal.add(tax).add(shipping);
    }
}