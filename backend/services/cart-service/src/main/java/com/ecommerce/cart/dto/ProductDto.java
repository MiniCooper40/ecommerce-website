package com.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product details fetched from catalog service.
 * Ignores unknown properties to maintain compatibility with catalog service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer stockQuantity;
    private String category;
    private String brand;
    private String imageUrl;
    private List<ImageDto> images;
    private Boolean active;
    
    /**
     * Get the primary image URL from the images list.
     * Falls back to imageUrl field if images is null or empty.
     */
    public String getImageUrl() {
        // If imageUrl is already set, return it
        if (this.imageUrl != null) {
            return this.imageUrl;
        }
        
        // Try to get primary image from images list
        if (this.images != null && !this.images.isEmpty()) {
            return this.images.stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .map(ImageDto::getUrl)
                    .findFirst()
                    .orElseGet(() -> this.images.get(0).getUrl());
        }
        
        return null;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageDto {
        private Long id;
        private String url;
        private String altText;
        private Boolean isPrimary;
        private Integer displayOrder;
    }
}
