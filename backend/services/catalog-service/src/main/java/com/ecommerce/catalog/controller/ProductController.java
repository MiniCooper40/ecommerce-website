package com.ecommerce.catalog.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

import com.ecommerce.catalog.dto.CreateProductRequest;
import com.ecommerce.catalog.dto.ProductDto;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.security.annotation.IsAdmin;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/catalog/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
        // Public endpoint - anyone can view products
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        // Public endpoint - anyone can view a specific product
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable String category) {
        // Public endpoint - anyone can browse by category
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @PostMapping
    @IsAdmin
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        // Only admins can create products
        return ResponseEntity.ok(productService.createProduct(productDto));
    }

    @PostMapping("/with-images")
    @IsAdmin
    public ResponseEntity<ProductDto> createProductWithImages(@Valid @RequestBody CreateProductRequest createRequest) {
        // Only admins can create products with images
        return ResponseEntity.ok(productService.createProduct(createRequest));
    }

    @PutMapping("/{id}")
    @IsAdmin
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) {
        // Only admins can update products
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
    }

    @DeleteMapping("/{id}")
    @IsAdmin
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // Only admins can delete products
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Enhanced endpoints using the rich service functionality

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam String q,
            Pageable pageable) {
        // Public endpoint - search products by name or description
        return ResponseEntity.ok(productService.searchProducts(q, pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDto>> getProductsWithFilters(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "false") boolean inStockOnly,
            Pageable pageable) {
        // Public endpoint - filter products by multiple criteria
        return ResponseEntity.ok(productService.getProductsWithFilters(
            category, brand, minPrice, maxPrice, inStockOnly, pageable));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        // Public endpoint - get all available categories
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        // Public endpoint - get all available brands
        return ResponseEntity.ok(productService.getAllBrands());
    }

    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductDto>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            Pageable pageable) {
        // Public endpoint - get products within price range
        return ResponseEntity.ok(productService.getProductsByPriceRange(minPrice, maxPrice, pageable));
    }

    // Admin endpoints for inventory management

    @GetMapping("/admin/low-stock")
    public ResponseEntity<List<ProductDto>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        // Admin only - get products with low stock
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }

    @PutMapping("/admin/{id}/stock")
    public ResponseEntity<ProductDto> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stockQuantity) {
        // Admin only - update product stock quantity
        return ResponseEntity.ok(productService.updateStock(id, stockQuantity));
    }

    @GetMapping("/admin/{id}/availability")
    public ResponseEntity<Boolean> checkProductAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        // Admin only - check if product has sufficient stock
        return ResponseEntity.ok(productService.isProductAvailable(id, quantity));
    }
}