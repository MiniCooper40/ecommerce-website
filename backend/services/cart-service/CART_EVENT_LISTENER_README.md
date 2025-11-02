# Cart Service Event Listener Implementation

## Overview

This implementation adds event-driven functionality to the cart service, allowing it to automatically update cart items when products are modified in the catalog service.

## Components Added

### 1. CartItemRepository Enhancement

- **File**: `src/main/java/com/ecommerce/cart/repository/CartItemRepository.java`
- **Added Method**: `List<CartItem> findByProductId(Long productId)`
- **Purpose**: Enables finding all cart items that contain a specific product

### 2. CartService Interface Update

- **File**: `src/main/java/com/ecommerce/cart/service/CartService.java`
- **Added Method**: `void updateCartItemsForProduct(Long productId, String productName, BigDecimal productPrice, String productImageUrl)`
- **Purpose**: Provides business logic for updating cart items based on product changes

### 3. CartService Implementation

- **File**: `src/main/java/com/ecommerce/cart/service/CartServiceImpl.java`
- **Implementation**: Updates all cart items for a given product ID with new product details
- **Features**:
  - Finds all cart items with the specified product ID
  - Updates product name, price, and image URL
  - Uses bulk save operation for efficiency

### 4. ProductEventListener

- **File**: `src/main/java/com/ecommerce/cart/config/ProductEventListener.java`
- **Purpose**: Kafka listener that consumes ProductUpdatedEvent messages
- **Features**:
  - Listens to "product-events" topic with "cart-service" consumer group
  - Logs event reception and processing
  - Handles exceptions gracefully
  - Provides correlation ID tracking

### 5. Dependencies Added

- **Spring Kafka**: Added to `pom.xml` for Kafka listener functionality
- **Events Library**: Already present for shared event types

## Event Flow

1. **Product Update**: A product is updated in the catalog service
2. **Event Publishing**: Catalog service publishes ProductUpdatedEvent to "product-events" topic
3. **Event Consumption**: Cart service ProductEventListener receives the event
4. **Cart Update**: All cart items containing that product are updated with new details
5. **Logging**: Success/failure is logged with correlation ID for tracing

## Key Features

### Asynchronous Processing

- Events are processed asynchronously, maintaining service independence
- Non-blocking operations ensure cart service performance

### Error Handling

- Comprehensive exception handling with logging
- Failed events are logged but don't crash the service
- Future enhancement: Could implement retry mechanisms or dead letter queues

### Data Consistency

- Ensures cart items stay in sync with product catalog
- Users see updated product information without manual refresh

### Scalability

- Consumer group allows multiple cart service instances
- Bulk update operations for efficiency

## Testing

### Unit Test

- **File**: `src/test/java/com/ecommerce/cart/config/ProductEventListenerTest.java`
- **Coverage**: Tests event listener functionality with mocked dependencies
- **Validation**: Ensures correct method calls with proper parameters

## Example Usage

When a product is updated in the catalog service:

```java
// In catalog service - publishes event
ProductUpdatedEvent event = ProductUpdatedEvent.builder()
    .productId("123")
    .name("Updated Product Name")
    .price(new BigDecimal("99.99"))
    .imageUrl("https://example.com/updated-image.jpg")
    .source("catalog-service")
    .correlationId(UUID.randomUUID().toString())
    .build();

eventPublisher.publish("product-events", event);
```

```java
// In cart service - automatically processes event
@KafkaListener(topics = "product-events", groupId = "cart-service")
public void handleProductUpdated(ProductUpdatedEvent event) {
    cartService.updateCartItemsForProduct(
        Long.valueOf(event.getAggregateId()),
        event.getName(),
        event.getPrice(),
        event.getImageUrl()
    );
}
```

## Configuration

The event listener automatically activates when:

- Kafka is running (docker-compose up)
- Cart service is started
- Events library is available in classpath

No additional configuration needed beyond existing Kafka setup.

## Benefits

1. **Real-time Synchronization**: Cart items update immediately when products change
2. **Improved User Experience**: Users see current product information
3. **Data Integrity**: Prevents stale product data in carts
4. **Microservice Decoupling**: Services communicate via events, not direct calls
5. **Audit Trail**: Correlation IDs enable event tracing across services

## Future Enhancements

1. **Retry Mechanisms**: Implement retry logic for failed event processing
2. **Dead Letter Queues**: Handle permanently failed events
3. **Event Versioning**: Support schema evolution
4. **Metrics**: Add monitoring for event processing performance
5. **Circuit Breaker**: Protect against downstream failures
