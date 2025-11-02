package com.ecommerce.catalog.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "brand")
    private String brand;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    @Builder.Default
    private List<Image> images = new ArrayList<>();

    @Builder.Default
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "weight", precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(name = "dimensions")
    private String dimensions;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Utility methods
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isAvailable() {
        return isActive != null && isActive && isInStock();
    }

    public void decreaseStock(Integer quantity) {
        if (quantity != null && stockQuantity != null && stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
        } else {
            throw new IllegalArgumentException("Insufficient stock");
        }
    }

    public void increaseStock(Integer quantity) {
        if (quantity != null && quantity > 0) {
            this.stockQuantity = (stockQuantity != null ? stockQuantity : 0) + quantity;
        }
    }

    // Image management methods
    public void addImage(Image image) {
        if (image != null) {
            image.setProduct(this);
            this.images.add(image);
        }
    }

    public void removeImage(Image image) {
        if (image != null) {
            this.images.remove(image);
            image.setProduct(null);
        }
    }

    public void clearImages() {
        this.images.forEach(image -> image.setProduct(null));
        this.images.clear();
    }

    public Optional<Image> getPrimaryImage() {
        return images.stream()
                .filter(Image::isAvailable)
                .filter(Image::getIsPrimary)
                .findFirst();
    }

    public List<Image> getActiveImages() {
        return images.stream()
                .filter(Image::isAvailable)
                .toList();
    }

    public boolean hasImages() {
        return !images.isEmpty() && images.stream().anyMatch(Image::isAvailable);
    }
}