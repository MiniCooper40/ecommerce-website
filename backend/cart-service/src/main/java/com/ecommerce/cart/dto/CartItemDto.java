package com.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    
    private Long id;
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Product name is required")
    private String productName;
    
    @NotNull(message = "Product price is required")
    private BigDecimal productPrice;
    
    private String productImageUrl;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Utility methods
    public BigDecimal getSubtotal() {
        if (productPrice != null && quantity != null) {
            return productPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}