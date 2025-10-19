package com.ecommerce.catalog.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.catalog.dto.ProductDto;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get all products with pagination
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(this::convertToDto);
    }

    // Get product by ID
    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not active");
        }
        
        return convertToDto(product);
    }

    // Get products by category
    public List<ProductDto> getProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategoryIgnoreCase(category)
                .stream()
                .filter(Product::getIsActive)
                .collect(Collectors.toList());
        
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Create new product (Admin only)
    public ProductDto createProduct(ProductDto productDto) {
        // Check if SKU already exists
        if (productDto.getSku() != null && productRepository.existsBySku(productDto.getSku())) {
            throw new RuntimeException("Product with SKU '" + productDto.getSku() + "' already exists");
        }

        Product product = convertToEntity(productDto);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        product = productRepository.save(product);
        return convertToDto(product);
    }

    // Update product (Admin only)
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Check if SKU conflict (if changing SKU)
        if (productDto.getSku() != null && 
            !productDto.getSku().equals(existingProduct.getSku()) && 
            productRepository.existsBySku(productDto.getSku())) {
            throw new RuntimeException("Product with SKU '" + productDto.getSku() + "' already exists");
        }

        // Update fields
        updateEntityFromDto(existingProduct, productDto);
        existingProduct.setUpdatedAt(LocalDateTime.now());
        
        existingProduct = productRepository.save(existingProduct);
        return convertToDto(existingProduct);
    }

    // Delete product (Admin only - soft delete by setting inactive)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    // Search products by name or description
    public List<ProductDto> searchProducts(String searchTerm) {
        List<Product> products = productRepository.searchProducts(searchTerm)
                .stream()
                .filter(Product::getIsActive)
                .collect(Collectors.toList());
        
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Search products with pagination
    public Page<ProductDto> searchProducts(String searchTerm, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(searchTerm, pageable);
        return products.map(this::convertToDto);
    }

    // Get products with filters
    public Page<ProductDto> getProductsWithFilters(
            String category, 
            String brand, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            boolean inStockOnly, 
            Pageable pageable) {
        
        Page<Product> products = productRepository.findProductsWithFilters(
            category, brand, minPrice, maxPrice, inStockOnly, true, pageable);
        
        return products.map(this::convertToDto);
    }

    // Get all categories
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    // Get all brands
    public List<String> getAllBrands() {
        return productRepository.findAllBrands();
    }

    // Get products by price range
    public Page<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByPriceRangeAndActive(minPrice, maxPrice, pageable);
        return products.map(this::convertToDto);
    }

    // Get low stock products (Admin only)
    public List<ProductDto> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Update stock quantity (for inventory management)
    public ProductDto updateStock(Long productId, Integer newStockQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        product.setStockQuantity(newStockQuantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        product = productRepository.save(product);
        return convertToDto(product);
    }

    // Check product availability and stock
    public boolean isProductAvailable(Long productId, Integer requestedQuantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return false;
        }
        
        Product product = productOpt.get();
        return product.getIsActive() && 
               product.getStockQuantity() != null && 
               product.getStockQuantity() >= requestedQuantity;
    }

    // Convert Entity to DTO
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setBrand(product.getBrand());
        dto.setImageUrl(product.getImageUrl());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setSku(product.getSku());
        dto.setWeight(product.getWeight());
        dto.setDimensions(product.getDimensions());
        dto.setIsActive(product.getIsActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    // Convert DTO to Entity
    private Product convertToEntity(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setBrand(dto.getBrand());
        product.setImageUrl(dto.getImageUrl());
        product.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        product.setSku(dto.getSku());
        product.setWeight(dto.getWeight());
        product.setDimensions(dto.getDimensions());
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return product;
    }

    // Update existing entity from DTO
    private void updateEntityFromDto(Product product, ProductDto dto) {
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getBrand() != null) product.setBrand(dto.getBrand());
        if (dto.getImageUrl() != null) product.setImageUrl(dto.getImageUrl());
        if (dto.getStockQuantity() != null) product.setStockQuantity(dto.getStockQuantity());
        if (dto.getSku() != null) product.setSku(dto.getSku());
        if (dto.getWeight() != null) product.setWeight(dto.getWeight());
        if (dto.getDimensions() != null) product.setDimensions(dto.getDimensions());
        if (dto.getIsActive() != null) product.setIsActive(dto.getIsActive());
    }
}