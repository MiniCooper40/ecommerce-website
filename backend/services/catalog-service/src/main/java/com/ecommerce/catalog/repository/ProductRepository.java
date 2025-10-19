package com.ecommerce.catalog.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.catalog.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find products by category
    List<Product> findByCategoryIgnoreCase(String category);
    
    // Find products by category with pagination
    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);
    
    // Find products by brand
    List<Product> findByBrandIgnoreCase(String brand);
    
    // Find products by name containing (case insensitive search)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Find products by price range
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Find products in stock
    List<Product> findByStockQuantityGreaterThan(Integer stockQuantity);
    
    // Find active products
    List<Product> findByIsActiveTrue();
    
    // Find active products with pagination
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    // Find by SKU (unique identifier)
    Optional<Product> findBySku(String sku);
    
    // Check if SKU exists
    boolean existsBySku(String sku);
    
    // Find products by multiple criteria with custom query
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR LOWER(p.category) = LOWER(:category)) AND " +
           "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:inStockOnly = false OR p.stockQuantity > 0) AND " +
           "(:activeOnly = false OR p.isActive = true)")
    Page<Product> findProductsWithFilters(
        @Param("category") String category,
        @Param("brand") String brand,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("inStockOnly") boolean inStockOnly,
        @Param("activeOnly") boolean activeOnly,
        Pageable pageable
    );
    
    // Search products by name or description (case insensitive)
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);
    
    // Search products with pagination
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Get all distinct categories
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.isActive = true ORDER BY p.category")
    List<String> findAllCategories();
    
    // Get all distinct brands
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.isActive = true AND p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findAllBrands();
    
    // Count products by category
    @Query("SELECT COUNT(p) FROM Product p WHERE LOWER(p.category) = LOWER(:category) AND p.isActive = true")
    Long countByCategory(@Param("category") String category);
    
    // Get low stock products (stock <= threshold)
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    // Get top selling products (this would require sales data integration)
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.id DESC")
    List<Product> findNewestProducts(Pageable pageable);
    
    // Get products by price range with active filter
    @Query("SELECT p FROM Product p WHERE " +
           "p.price BETWEEN :minPrice AND :maxPrice AND " +
           "p.isActive = true")
    Page<Product> findByPriceRangeAndActive(
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice, 
        Pageable pageable
    );
}