package com.ecommerce.catalog.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.catalog.dto.CreateImageRequest;
import com.ecommerce.catalog.dto.CreateProductRequest;
import com.ecommerce.catalog.dto.ImageDto;
import com.ecommerce.catalog.dto.ProductDto;
import com.ecommerce.catalog.service.ProductService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductImageIntegrationTest {

    @Autowired
    private ProductService productService;

    @Test
    public void testCreateProductWithImages() {
        // Create image requests (simulating images uploaded via presigned URLs)
        CreateImageRequest image1 = new CreateImageRequest();
        image1.setS3Key("products/test-product-1.jpg");
        image1.setAltText("Main product image");
        image1.setDisplayOrder(0);
        image1.setIsPrimary(true);

        CreateImageRequest image2 = new CreateImageRequest();
        image2.setS3Key("products/test-product-2.jpg");
        image2.setAltText("Secondary product image");
        image2.setDisplayOrder(1);
        image2.setIsPrimary(false);

        List<CreateImageRequest> images = Arrays.asList(image1, image2);

        // Create product request
        CreateProductRequest createRequest = new CreateProductRequest();
        createRequest.setName("Test Product with Images");
        createRequest.setDescription("A product to test image functionality");
        createRequest.setPrice(new BigDecimal("29.99"));
        createRequest.setCategory("Electronics");
        createRequest.setBrand("TestBrand");
        createRequest.setStockQuantity(10);
        createRequest.setSku("TEST-IMAGES-001");
        createRequest.setImages(images);

        // Create the product
        ProductDto createdProduct = productService.createProduct(createRequest);

        // Verify the product was created correctly
        assertNotNull(createdProduct);
        assertNotNull(createdProduct.getId());
        assertEquals("Test Product with Images", createdProduct.getName());
        assertEquals(new BigDecimal("29.99"), createdProduct.getPrice());

        // Verify images were created and associated
        List<ImageDto> productImages = createdProduct.getImages();
        assertNotNull(productImages);
        assertEquals(2, productImages.size());

        // Verify primary image
        ImageDto primaryImage = productImages.stream()
                .filter(ImageDto::getIsPrimary)
                .findFirst()
                .orElse(null);
        assertNotNull(primaryImage);
        assertEquals("products/test-product-1.jpg", primaryImage.getS3Key());
        assertEquals("Main product image", primaryImage.getAltText());
        assertEquals(0, primaryImage.getDisplayOrder());

        // Verify secondary image
        ImageDto secondaryImage = productImages.stream()
                .filter(img -> !img.getIsPrimary())
                .findFirst()
                .orElse(null);
        assertNotNull(secondaryImage);
        assertEquals("products/test-product-2.jpg", secondaryImage.getS3Key());
        assertEquals("Secondary product image", secondaryImage.getAltText());
        assertEquals(1, secondaryImage.getDisplayOrder());

        // Verify images are active
        assertTrue(productImages.stream().allMatch(ImageDto::getIsActive));
    }

    @Test
    public void testCreateProductWithoutImages() {
        // Create product request without images
        CreateProductRequest createRequest = new CreateProductRequest();
        createRequest.setName("Test Product without Images");
        createRequest.setDescription("A product without images");
        createRequest.setPrice(new BigDecimal("19.99"));
        createRequest.setCategory("Books");
        createRequest.setBrand("TestPublisher");
        createRequest.setStockQuantity(5);
        createRequest.setSku("TEST-NO-IMAGES-001");
        // No images set

        // Create the product
        ProductDto createdProduct = productService.createProduct(createRequest);

        // Verify the product was created correctly
        assertNotNull(createdProduct);
        assertNotNull(createdProduct.getId());
        assertEquals("Test Product without Images", createdProduct.getName());

        // Verify no images
        List<ImageDto> productImages = createdProduct.getImages();
        assertNotNull(productImages);
        assertTrue(productImages.isEmpty());
    }
}