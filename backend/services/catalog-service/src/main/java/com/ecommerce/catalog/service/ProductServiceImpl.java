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

import com.ecommerce.catalog.dto.CreateProductRequest;
import com.ecommerce.catalog.dto.ImageDto;
import com.ecommerce.catalog.dto.ProductDto;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.shared.events.EventPublisher;
import com.ecommerce.shared.events.domain.ProductDeletedEvent;
import com.ecommerce.shared.events.domain.ProductUpdatedEvent;
import com.ecommerce.shared.events.util.EventCorrelationUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private EventPublisher eventPublisher;

    // Get all products with pagination
    @Override
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(this::convertToDto);
    }

    // Get product by ID
    @Override
    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not active");
        }
        
        return convertToDto(product);
    }

    // Get products by category
    @Override
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
    @Override
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

    // Create new product with images (Admin only)
    @Override
    public ProductDto createProduct(CreateProductRequest createRequest) {
        // Check if SKU already exists
        if (createRequest.getSku() != null && productRepository.existsBySku(createRequest.getSku())) {
            throw new RuntimeException("Product with SKU '" + createRequest.getSku() + "' already exists");
        }

        Product product = convertToEntity(createRequest);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);

        // Create images if provided
        if (createRequest.getImages() != null && !createRequest.getImages().isEmpty()) {
            createRequest.getImages().forEach(imageRequest -> {
                imageService.createImage(savedProduct, imageRequest);
            });
        }

        // Reload product with images
        Product reloadedProduct = productRepository.findById(savedProduct.getId()).orElse(savedProduct);
        return convertToDto(reloadedProduct);
    }

    // Update product (Admin only)
    @Override
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
        
        // Publish ProductUpdatedEvent
        publishProductUpdatedEvent(existingProduct);
        
        return convertToDto(existingProduct);
    }

    // Delete product (Admin only - soft delete by setting inactive)
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        // Publish ProductDeletedEvent to notify other services
        publishProductDeletedEvent(product);
    }

    // Search products by name or description
    @Override
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
    @Override
    public Page<ProductDto> searchProducts(String searchTerm, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(searchTerm, pageable);
        return products.map(this::convertToDto);
    }

    // Get products with filters
    @Override
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
    @Override
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    // Get all brands
    @Override
    public List<String> getAllBrands() {
        return productRepository.findAllBrands();
    }

    // Get products by price range
    @Override
    public Page<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByPriceRangeAndActive(minPrice, maxPrice, pageable);
        return products.map(this::convertToDto);
    }

    // Get low stock products (Admin only)
    @Override
    public List<ProductDto> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Update stock quantity (for inventory management)
    @Override
    public ProductDto updateStock(Long productId, Integer newStockQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        product.setStockQuantity(newStockQuantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        product = productRepository.save(product);
        
        // Publish ProductUpdatedEvent for stock changes
        publishProductUpdatedEvent(product);
        
        return convertToDto(product);
    }

    // Check product availability and stock
    @Override
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
        
        // Convert images
        List<ImageDto> imageDtos = imageService.getImagesByProductId(product.getId());
        dto.setImages(imageDtos);
        
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
        product.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        product.setSku(dto.getSku());
        product.setWeight(dto.getWeight());
        product.setDimensions(dto.getDimensions());
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return product;
    }

    // Convert CreateProductRequest to Entity
    private Product convertToEntity(CreateProductRequest dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setBrand(dto.getBrand());
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
        if (dto.getStockQuantity() != null) product.setStockQuantity(dto.getStockQuantity());
        if (dto.getSku() != null) product.setSku(dto.getSku());
        if (dto.getWeight() != null) product.setWeight(dto.getWeight());
        if (dto.getDimensions() != null) product.setDimensions(dto.getDimensions());
        if (dto.getIsActive() != null) product.setIsActive(dto.getIsActive());
        // Note: Image updates should be handled separately through ImageService
    }

    /**
     * Publishes a ProductUpdatedEvent for the given product
     */
    private void publishProductUpdatedEvent(Product product) {
        try {
            // Get primary image URL if available
            String imageUrl = product.getPrimaryImage()
                    .map(image -> image.getUrl())
                    .orElse(null);

            ProductUpdatedEvent event = ProductUpdatedEvent.builder()
                    .productId(product.getId().toString())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .currency("USD") // Default currency, could be configurable
                    .stockQuantity(product.getStockQuantity())
                    .category(product.getCategory())
                    .imageUrl(imageUrl)
                    .active(product.getIsActive())
                    .source("catalog-service")
                    .correlationId(EventCorrelationUtils.getOrCreateCorrelationId())
                    .build();

            eventPublisher.publish(event);
            
            log.info("Published ProductUpdatedEvent for product ID: {} with correlation ID: {}", 
                    product.getId(), event.getCorrelationId());
                    
        } catch (Exception e) {
            log.error("Failed to publish ProductUpdatedEvent for product ID: {}", product.getId(), e);
            // Don't rethrow the exception to avoid breaking the main operation
        }
    }

    /**
     * Publishes a ProductDeletedEvent when a product is soft-deleted
     */
    private void publishProductDeletedEvent(Product product) {
        try {
            ProductDeletedEvent event = ProductDeletedEvent.builder()
                    .productId(product.getId().toString())
                    .name(product.getName())
                    .category(product.getCategory())
                    .source("catalog-service")
                    .correlationId(EventCorrelationUtils.getOrCreateCorrelationId())
                    .build();

            eventPublisher.publish(event);
            
            log.info("Published ProductDeletedEvent for product ID: {} with correlation ID: {}", 
                    product.getId(), event.getCorrelationId());
                    
        } catch (Exception e) {
            log.error("Failed to publish ProductDeletedEvent for product ID: {}", product.getId(), e);
            // Don't rethrow the exception to avoid breaking the main operation
        }
    }
}