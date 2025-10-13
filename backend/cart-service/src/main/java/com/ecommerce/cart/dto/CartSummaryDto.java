package com.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartSummaryDto {
    
    private List<CartItemDto> items;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shipping;
    private BigDecimal total;

    // Constructors
    public CartSummaryDto() {}

    public CartSummaryDto(List<CartItemDto> items) {
        this.items = items;
        this.calculateTotals();
    }

    // Getters and Setters
    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
        this.calculateTotals();
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getShipping() {
        return shipping;
    }

    public void setShipping(BigDecimal shipping) {
        this.shipping = shipping;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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