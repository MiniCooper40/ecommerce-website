package com.ecommerce.catalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.catalog.dto.ProductDto;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.shared.testutil.WithMockUserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllProducts_Public() throws Exception {
        // Arrange
        ProductDto product = createSampleProduct();
        Page<ProductDto> productPage = new PageImpl<>(Arrays.asList(product), 
            PageRequest.of(0, 10), 1);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    public void testGetProduct_Public() throws Exception {
        // Arrange
        ProductDto product = createSampleProduct();
        when(productService.getProduct(1L)).thenReturn(product);

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUserPrincipal(userId = "admin-user", roles = {"ADMIN"})
    public void testCreateProduct_AdminUser() throws Exception {
        // Arrange
        ProductDto product = createSampleProduct();
        when(productService.createProduct(any(ProductDto.class))).thenReturn(product);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUserPrincipal(userId = "regular-user", roles = {"USER"})
    public void testCreateProduct_RegularUser_Forbidden() throws Exception {
        // Arrange
        ProductDto product = createSampleProduct();

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
    }

    private ProductDto createSampleProduct() {
        ProductDto product = new ProductDto();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("29.99"));
        product.setCategory("Electronics");
        product.setBrand("TestBrand");
        product.setStockQuantity(5);
        // Images will be empty list for this test
        product.setImages(new ArrayList<>());
        return product;
    }
}