package com.ecommerce.catalog.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ecommerce.catalog.dto.CreateImageRequest;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageService imageService;

    @Override
    public void run(String... args) throws Exception {
        initializeSampleProducts();
    }

    private void initializeSampleProducts() {
        // Only initialize if database is empty
        if (productRepository.count() > 0) {
            log.info("Products already exist in database, skipping initialization");
            return;
        }

        log.info("Initializing sample products...");

        List<Product> products = new ArrayList<>();

        // Electronics Category
        products.add(createProduct(
            "Wireless Bluetooth Headphones",
            "Premium noise-canceling over-ear headphones with 30-hour battery life and superior sound quality.",
            new BigDecimal("149.99"),
            "Electronics",
            "AudioTech",
            "WBH-3000",
            new BigDecimal("0.5"),
            "25 x 20 x 10 cm",
            50,
            "/images/products/headphones-1.jpg",
            "/images/products/headphones-2.jpg"
        ));

        products.add(createProduct(
            "Smart Fitness Watch",
            "Advanced fitness tracker with heart rate monitor, GPS, and 7-day battery life. Water-resistant up to 50m.",
            new BigDecimal("249.99"),
            "Electronics",
            "FitTrack",
            "SFW-200",
            new BigDecimal("0.15"),
            "4.5 x 4.5 x 1.2 cm",
            75,
            "/images/products/fitness-watch-1.jpg",
            "/images/products/fitness-watch-2.jpg",
            "/images/products/fitness-watch-3.jpg"
        ));

        products.add(createProduct(
            "4K Webcam Pro",
            "Professional 4K webcam with autofocus, dual microphones, and adjustable tripod mount. Perfect for streaming and video calls.",
            new BigDecimal("99.99"),
            "Electronics",
            "StreamTech",
            "WCP-4K",
            new BigDecimal("0.3"),
            "10 x 8 x 8 cm",
            40,
            "/images/products/webcam-1.jpg",
            "/images/products/webcam-2.jpg"
        ));

        // Home & Kitchen Category
        products.add(createProduct(
            "Stainless Steel Coffee Maker",
            "Programmable 12-cup coffee maker with thermal carafe, brew strength control, and auto-shutoff.",
            new BigDecimal("79.99"),
            "Home & Kitchen",
            "BrewMaster",
            "SSCM-12",
            new BigDecimal("2.5"),
            "35 x 20 x 32 cm",
            30,
            "/images/products/coffee-maker-1.jpg",
            "/images/products/coffee-maker-2.jpg"
        ));

        products.add(createProduct(
            "Non-Stick Cookware Set",
            "10-piece professional cookware set with non-stick coating, heat-resistant handles, and tempered glass lids.",
            new BigDecimal("199.99"),
            "Home & Kitchen",
            "ChefPro",
            "NSCS-10",
            new BigDecimal("8.0"),
            "45 x 35 x 25 cm",
            25,
            "/images/products/cookware-1.jpg",
            "/images/products/cookware-2.jpg",
            "/images/products/cookware-3.jpg"
        ));

        products.add(createProduct(
            "Robot Vacuum Cleaner",
            "Smart robot vacuum with app control, automatic charging, and multi-surface cleaning. Works with Alexa and Google Home.",
            new BigDecimal("299.99"),
            "Home & Kitchen",
            "CleanBot",
            "RVC-360",
            new BigDecimal("3.5"),
            "35 x 35 x 10 cm",
            20,
            "/images/products/robot-vacuum-1.jpg",
            "/images/products/robot-vacuum-2.jpg"
        ));

        // Sports & Outdoors Category
        products.add(createProduct(
            "Yoga Mat Premium",
            "Extra thick (6mm) non-slip yoga mat with carrying strap. Eco-friendly TPE material.",
            new BigDecimal("39.99"),
            "Sports & Outdoors",
            "ZenFit",
            "YMP-6MM",
            new BigDecimal("1.2"),
            "183 x 61 x 0.6 cm",
            100,
            "/images/products/yoga-mat-1.jpg",
            "/images/products/yoga-mat-2.jpg",
            "/images/products/yoga-mat-3.jpg"
        ));

        products.add(createProduct(
            "Adjustable Dumbbells Set",
            "Space-saving adjustable dumbbell set, 5-52.5 lbs per hand. Quick-select weight adjustment system.",
            new BigDecimal("349.99"),
            "Sports & Outdoors",
            "PowerLift",
            "ADS-52",
            new BigDecimal("25.0"),
            "40 x 20 x 20 cm",
            15,
            "/images/products/dumbbells-1.jpg",
            "/images/products/dumbbells-2.jpg"
        ));

        products.add(createProduct(
            "Camping Tent 4-Person",
            "Waterproof 4-person tent with easy setup, ventilation windows, and storage pockets. Includes carrying bag.",
            new BigDecimal("159.99"),
            "Sports & Outdoors",
            "OutdoorPro",
            "CT-4P",
            new BigDecimal("6.5"),
            "220 x 180 x 140 cm",
            12,
            "/images/products/tent-1.jpg",
            "/images/products/tent-2.jpg",
            "/images/products/tent-3.jpg"
        ));

        // Books & Media Category
        products.add(createProduct(
            "The Art of Programming",
            "Comprehensive guide to software development best practices and design patterns. 500+ pages.",
            new BigDecimal("49.99"),
            "Books & Media",
            "TechPress",
            "TAP-2024",
            new BigDecimal("1.0"),
            "23 x 15 x 3 cm",
            200,
            "/images/products/book-programming-1.jpg",
            "/images/products/book-programming-2.jpg"
        ));

        // Fashion Category
        products.add(createProduct(
            "Classic Leather Backpack",
            "Premium genuine leather backpack with laptop compartment, multiple pockets, and adjustable straps.",
            new BigDecimal("129.99"),
            "Fashion",
            "UrbanStyle",
            "CLB-BRN",
            new BigDecimal("1.5"),
            "45 x 30 x 15 cm",
            35,
            "/images/products/backpack-1.jpg",
            "/images/products/backpack-2.jpg",
            "/images/products/backpack-3.jpg"
        ));

        products.add(createProduct(
            "Running Shoes Pro",
            "Lightweight running shoes with responsive cushioning and breathable mesh upper. Available in multiple colors.",
            new BigDecimal("119.99"),
            "Fashion",
            "SpeedRunner",
            "RSP-9000",
            new BigDecimal("0.6"),
            "31 x 20 x 12 cm",
            60,
            "/images/products/running-shoes-1.jpg",
            "/images/products/running-shoes-2.jpg",
            "/images/products/running-shoes-3.jpg"
        ));

        // Save all products
        for (Product product : products) {
            Product savedProduct = productRepository.save(product);
            log.info("Created product: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
        }

        log.info("Successfully initialized {} sample products", products.size());
        log.info("=====================================================");
        log.info("Sample products created across categories:");
        log.info("  - Electronics: 3 products");
        log.info("  - Home & Kitchen: 3 products");
        log.info("  - Sports & Outdoors: 3 products");
        log.info("  - Books & Media: 1 product");
        log.info("  - Fashion: 2 products");
        log.info("=====================================================");
    }

    private Product createProduct(String name, String description, BigDecimal price,
                                 String category, String brand, String sku,
                                 BigDecimal weight, String dimensions, Integer stockQuantity,
                                 String... imagePaths) {
        
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .brand(brand)
                .sku(sku)
                .weight(weight)
                .dimensions(dimensions)
                .stockQuantity(stockQuantity)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        product = productRepository.save(product);

        // Create images for the product
        if (imagePaths != null && imagePaths.length > 0) {
            for (int i = 0; i < imagePaths.length; i++) {
                try {
                    // Convert /images/products/filename.jpg -> products/filename.jpg (S3 key format)
                    String s3Key = imagePaths[i].startsWith("/images/") 
                        ? imagePaths[i].substring("/images/".length())
                        : imagePaths[i];
                    
                    CreateImageRequest imageRequest = new CreateImageRequest();
                    imageRequest.setS3Key(s3Key);
                    imageRequest.setAltText(name + " - Image " + (i + 1));
                    imageRequest.setDisplayOrder(i);
                    imageRequest.setIsPrimary(i == 0); // First image is primary
                    
                    imageService.createImage(product, imageRequest);
                    log.debug("  - Created image: {}", s3Key);
                } catch (Exception e) {
                    log.warn("Failed to create image for product {}: {}", name, e.getMessage());
                }
            }
        }

        return product;
    }
}
