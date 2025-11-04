package com.ecommerce.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ecommerce.cart.dto.ProductDto;

/**
 * Feign client for communicating with the catalog service.
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/catalog/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);
}
