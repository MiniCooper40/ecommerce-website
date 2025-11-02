# Example Usage Guide: Implementing Kafka Events in Catalog Service

This guide shows how to use the events library in the catalog service to publish product update events.

## 1. Dependencies Added

The catalog service already has the events library dependency in its `pom.xml`:

```xml
<!-- Events Library -->
<dependency>
    <groupId>com.ecommerce</groupId>
    <artifactId>events-lib</artifactId>
</dependency>
```

## 2. Configuration Added

Added Kafka configuration to `application.yml`:

```yaml
# Events Configuration
ecommerce:
  events:
    bootstrap-servers: localhost:9092
    topics:
      product-events: product-events
      cart-events: cart-events
      order-events: order-events

---
spring:
  config:
    activate:
      on-profile: docker

# Events Configuration for Docker
ecommerce:
  events:
    bootstrap-servers: kafka:9093
    topics:
      product-events: product-events
      cart-events: cart-events
      order-events: order-events
```

## 3. Service Implementation

Modified `ProductServiceImpl.java` to publish events:

### Key Changes:

1. **Added imports and dependencies:**

```java
import com.ecommerce.shared.events.EventPublisher;
import com.ecommerce.shared.events.domain.ProductUpdatedEvent;
import com.ecommerce.shared.events.util.EventCorrelationUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private EventPublisher eventPublisher;
    // ... other dependencies
}
```

2. **Modified update methods to publish events:**

```java
// Update product (Admin only)
@Override
public ProductDto updateProduct(Long id, ProductDto productDto) {
    // ... existing update logic

    existingProduct = productRepository.save(existingProduct);

    // Publish ProductUpdatedEvent
    publishProductUpdatedEvent(existingProduct);

    return convertToDto(existingProduct);
}

// Update stock quantity (for inventory management)
@Override
public ProductDto updateStock(Long productId, Integer newStockQuantity) {
    // ... existing update logic

    product = productRepository.save(product);

    // Publish ProductUpdatedEvent for stock changes
    publishProductUpdatedEvent(product);

    return convertToDto(product);
}
```

3. **Added event publishing helper method:**

```java
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
```

## 4. Event Handler Example

Created `ProductEventHandler.java` to show how to consume events:

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventHandler {

    private final ProductService productService;
    private final EventPublisher eventPublisher;

    /**
     * Handle product validation requests from other services
     */
    @KafkaListener(topics = "product-events", groupId = "catalog-service-validation")
    public void handleProductValidationRequest(ProductValidationRequestedEvent event, Acknowledgment ack) {
        try {
            log.info("Received product validation request for products: {}", event.getProductIds());

            // Validate products
            List<String> validProducts = event.getProductIds().stream()
                    .filter(productId -> {
                        try {
                            Long id = Long.parseLong(productId);
                            return productService.isProductAvailable(id, 1);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            // Send response
            ProductValidationCompletedEvent response = ProductValidationCompletedEvent.builder()
                    .requestId(event.getAggregateId())
                    .validProducts(validProducts)
                    .isValid(!validProducts.isEmpty())
                    .requestingService(event.getRequestingService())
                    .source("catalog-service")
                    .correlationId(event.getCorrelationId())
                    .build();

            eventPublisher.publish(response);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process product validation request", e);
        }
    }
}
```

## 5. How to Test

### Start the Infrastructure:

```bash
cd dev
docker-compose up -d
```

### Build and Run the Services:

```bash
cd backend
mvn clean install
```

### Test the Events:

1. **Update a product via REST API:**

```bash
curl -X PUT http://localhost:8082/api/products/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "name": "Updated Product Name",
    "price": 29.99,
    "stockQuantity": 100
  }'
```

2. **Check Kafka UI for events:**

   - Open http://localhost:8080
   - Navigate to Topics → product-events
   - View messages to see the published `ProductUpdatedEvent`

3. **Monitor logs:**

```bash
# Watch catalog service logs
docker logs -f catalog-service

# You should see:
# Published ProductUpdatedEvent for product ID: 1 with correlation ID: abc-123-def
```

## 6. Event Flow for Saga Pattern

Here's how this enables saga patterns:

### Order Creation Saga:

1. **Order Service** creates order → publishes `OrderCreatedEvent`
2. **Cart Service** validates cart → publishes `CartValidationCompletedEvent`
3. **Catalog Service** validates products → publishes `ProductValidationCompletedEvent`
4. **Order Service** processes results and completes/cancels order

### Example Event Flow:

```
Order Service: OrderCreatedEvent
    ↓
Cart Service: CartValidationCompletedEvent (valid=true)
    ↓
Catalog Service: ProductValidationCompletedEvent (valid=true)
    ↓
Order Service: OrderCompletedEvent
```

## 7. Benefits Achieved

1. **Loose Coupling**: Services communicate via events, not direct API calls
2. **Resilience**: Failed event processing doesn't break the main operation
3. **Scalability**: Events can be processed asynchronously
4. **Traceability**: Correlation IDs enable distributed tracing
5. **Flexibility**: Easy to add new event consumers without changing producers

## 8. Next Steps

To complete the implementation:

1. **Build the events library:** `mvn clean install` in the backend directory
2. **Add similar event publishing** to other services (cart, order)
3. **Implement event handlers** in consuming services
4. **Add monitoring** and alerting for event processing
5. **Add error handling** and dead letter queues for failed events

This implementation provides a solid foundation for building event-driven microservices with proper saga pattern support!
