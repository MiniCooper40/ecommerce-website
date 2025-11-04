package com.ecommerce.cart.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ecommerce.cart.client.CatalogClient;
import com.ecommerce.cart.dto.ProductDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for fetching and caching product details.
 */
@Service
@Slf4j
public class ProductCacheService {

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final long CACHE_TTL_HOURS = 1;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CatalogClient catalogClient;

    /**
     * Get product details from cache or fetch from catalog service.
     */
    public ProductDto getProduct(Long productId) {
        log.info("Fetching product details for productId: {}", productId);

        // Try to get from Redis cache first
        String cacheKey = PRODUCT_CACHE_PREFIX + productId;
        ProductDto cachedProduct = (ProductDto) redisTemplate.opsForValue().get(cacheKey);

        if (cachedProduct != null) {
            log.info("Product found in cache: {}", productId);
            return cachedProduct;
        }

        // Fetch from catalog service if not in cache
        log.info("Product not in cache, fetching from catalog service: {}", productId);
        ProductDto product = catalogClient.getProductById(productId);

        if (product != null) {
            // Cache the product
            redisTemplate.opsForValue().set(cacheKey, product, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("Cached product: {}", productId);
        }

        return product;
    }

    /**
     * Invalidate product cache when product is updated.
     */
    public void invalidateProductCache(Long productId) {
        String cacheKey = PRODUCT_CACHE_PREFIX + productId;
        redisTemplate.delete(cacheKey);
        log.info("Invalidated cache for product: {}", productId);
    }

    /**
     * Update product in cache.
     */
    public void updateProductCache(ProductDto product) {
        if (product != null && product.getId() != null) {
            String cacheKey = PRODUCT_CACHE_PREFIX + product.getId();
            redisTemplate.opsForValue().set(cacheKey, product, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("Updated cache for product: {}", product.getId());
        }
    }
}
