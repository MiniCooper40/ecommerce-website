package com.ecommerce.catalog.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.catalog.dto.ProductDto;

/**
 * Interface for product service operations
 */
public interface ProductService {

    /**
     * Get all products with pagination
     */
    Page<ProductDto> getAllProducts(Pageable pageable);

    /**
     * Get product by ID
     */
    ProductDto getProduct(Long id);

    /**
     * Get products by category
     */
    List<ProductDto> getProductsByCategory(String category);

    /**
     * Create new product (Admin only)
     */
    ProductDto createProduct(ProductDto productDto);

    /**
     * Update product (Admin only)
     */
    ProductDto updateProduct(Long id, ProductDto productDto);

    /**
     * Delete product (Admin only - soft delete by setting inactive)
     */
    void deleteProduct(Long id);

    /**
     * Search products by name or description
     */
    List<ProductDto> searchProducts(String searchTerm);

    /**
     * Search products with pagination
     */
    Page<ProductDto> searchProducts(String searchTerm, Pageable pageable);

    /**
     * Get products with filters
     */
    Page<ProductDto> getProductsWithFilters(
            String category, 
            String brand, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            boolean inStockOnly, 
            Pageable pageable);

    /**
     * Get all categories
     */
    List<String> getAllCategories();

    /**
     * Get all brands
     */
    List<String> getAllBrands();

    /**
     * Get products by price range
     */
    Page<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Get low stock products (Admin only)
     */
    List<ProductDto> getLowStockProducts(Integer threshold);

    /**
     * Update stock quantity (for inventory management)
     */
    ProductDto updateStock(Long productId, Integer newStockQuantity);

    /**
     * Check product availability and stock
     */
    boolean isProductAvailable(Long productId, Integer requestedQuantity);
}