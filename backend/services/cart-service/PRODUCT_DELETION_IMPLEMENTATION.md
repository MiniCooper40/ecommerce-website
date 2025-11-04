# Product Deletion Event Handling Implementation

## Overview

This document describes the implementation of product deletion event handling in the e-commerce microservices system. When a product is deleted (soft-deleted) in the catalog service, cart items containing that product are marked as unavailable, requiring users to manually remove them from their cart.

## Design Decision: Mark as Unavailable vs Auto-Remove

**Chosen Approach: Mark as Unavailable**

We chose to mark cart items as unavailable (using an `available` flag) rather than automatically removing them. This provides better UX because:

1. **User Awareness**: Users are explicitly shown that a product they added is no longer available
2. **Transparency**: Users can see what was removed and why
3. **Decision Control**: Users maintain control over their cart and can make informed decisions
4. **Audit Trail**: The system retains a record of what products were in the cart

**Alternative Considered: Saga Pattern / Compensating Transaction**

A compensating event/saga pattern could have been used where:

- Cart service listens for ProductDeletedEvent
- Automatically removes cart items
- Publishes CartItemsRemovedEvent for audit

However, this approach was rejected because:

- Silent removal provides poor UX (users don't know what happened)
- Loss of context (users forget what they had in cart)
- Unnecessary complexity for this use case

## Architecture

### Event Flow

```
1. Admin deletes product in Catalog Service
   ↓
2. Catalog Service publishes ProductDeletedEvent to Kafka
   ↓
3. Cart Service's ProductEventListener receives event
   ↓
4. Cart Service marks all CartItemView records as unavailable
   ↓
5. Users see unavailable items in their cart (available=false)
   ↓
6. Users manually remove unavailable items
```

### Components Modified

#### 1. Events Library (Shared)

**File**: `backend/shared/events-lib/src/main/java/com/ecommerce/shared/events/domain/ProductDeletedEvent.java`

```java
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductDeletedEvent extends BaseEvent {
    @JsonProperty("name")
    private String name;

    @JsonProperty("category")
    private String category;
}
```

**Purpose**: Represents the event when a product is soft-deleted
**Topic**: `product-events`

#### 2. Cart Service - Entity Layer

**File**: `backend/services/cart-service/src/main/java/com/ecommerce/cart/entity/CartItemView.java`

**Changes**:

- Added `Boolean available` field (default: true)
- Column: `available BOOLEAN NOT NULL DEFAULT true`

```java
@Column(name = "available", nullable = false)
@Builder.Default
private Boolean available = true;
```

#### 3. Cart Service - DTO Layer

**File**: `backend/services/cart-service/src/main/java/com/ecommerce/cart/dto/CartItemDto.java`

**Changes**:

- Added `Boolean available` field to expose availability status in API responses

```java
@Builder.Default
private Boolean available = true;
```

#### 4. Cart Service - Repository Layer

**File**: `backend/services/cart-service/src/main/java/com/ecommerce/cart/repository/CartItemViewRepository.java`

**New Methods**:

```java
@Modifying
@Query("UPDATE CartItemView v SET v.available = false WHERE v.productId = :productId")
void markProductAsUnavailable(@Param("productId") Long productId);

@Modifying
@Query("DELETE FROM CartItemView v WHERE v.productId = :productId AND v.available = false")
void deleteUnavailableProductItems(@Param("productId") Long productId);
```

**Purpose**:

- `markProductAsUnavailable`: Marks all cart items with a specific product as unavailable
- `deleteUnavailableProductItems`: Cleanup method for removing unavailable items (can be used in batch jobs)

#### 5. Cart Service - Query Service

**File**: `backend/services/cart-service/src/main/java/com/ecommerce/cart/service/CartQueryService.java`

**Changes**:

- Updated `convertToDto` method to map the `available` field from entity to DTO

#### 6. Cart Service - Event Listener

**File**: `backend/services/cart-service/src/main/java/com/ecommerce/cart/listener/ProductEventListener.java`

**New Handler**:

```java
@KafkaHandler
@Transactional
public void handleProductDeleted(ProductDeletedEvent event) {
    Long productId = Long.parseLong(event.getAggregateId());

    // Mark all cart items as unavailable
    cartItemViewRepository.markProductAsUnavailable(productId);

    // Invalidate cache
    productCacheService.invalidateProductCache(productId);

    log.info("Marked all CartItemViews as unavailable for deleted productId: {}", productId);
}
```

**Purpose**:

- Listens to `ProductDeletedEvent` from Kafka topic `product-events`
- Marks cart items as unavailable (doesn't delete them)
- Removes product from Redis cache

#### 7. Catalog Service - Service Layer

**File**: `backend/services/catalog-service/src/main/java/com/ecommerce/catalog/service/ProductServiceImpl.java`

**Changes**:

1. Added ProductDeletedEvent import
2. Updated `deleteProduct` method to publish event:

```java
@Override
public void deleteProduct(Long id) {
    Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

    product.setIsActive(false);
    product.setUpdatedAt(LocalDateTime.now());
    productRepository.save(product);

    // Publish ProductDeletedEvent
    publishProductDeletedEvent(product);
}
```

3. Added new method `publishProductDeletedEvent`:

```java
private void publishProductDeletedEvent(Product product) {
    ProductDeletedEvent event = ProductDeletedEvent.builder()
            .productId(product.getId().toString())
            .name(product.getName())
            .category(product.getCategory())
            .source("catalog-service")
            .correlationId(EventCorrelationUtils.getOrCreateCorrelationId())
            .build();

    eventPublisher.publish(event);
}
```

#### 8. Database Migration

**File**: `backend/services/cart-service/src/main/resources/db/migration/V2__add_available_column.sql`

```sql
-- Add available column with default value of true
ALTER TABLE cart_item_view
ADD COLUMN IF NOT EXISTS available BOOLEAN NOT NULL DEFAULT true;

-- Add index on available column for efficient filtering
CREATE INDEX IF NOT EXISTS idx_cart_view_available ON cart_item_view(available);
```

**Purpose**: Adds the `available` column to existing databases

## API Changes

### GET /api/cart

**Response Schema Change**:

```json
{
  "items": [
    {
      "id": 123,
      "productId": 456,
      "productName": "Example Product",
      "productPrice": 99.99,
      "quantity": 2,
      "available": false, // NEW FIELD
      "createdAt": "2025-11-04T10:00:00",
      "updatedAt": "2025-11-04T11:00:00"
    }
  ],
  "totalItems": 2,
  "subtotal": 199.98
}
```

**Frontend Impact**:

- Frontend should check `available` field
- Display unavailable items with visual indication (grayed out, crossed out, warning badge)
- Prevent checkout if cart contains unavailable items
- Show "Remove unavailable items" button or auto-filter option

## Event Schema

### ProductDeletedEvent

**Topic**: `product-events`  
**Consumer Group**: `cart-service`

```json
{
  "aggregateId": "123",
  "aggregateType": "Product",
  "eventType": "ProductDeletedEvent",
  "timestamp": "2025-11-04T10:30:00Z",
  "source": "catalog-service",
  "correlationId": "uuid-here",
  "name": "Example Product",
  "category": "Electronics"
}
```

## Testing Strategy

### Unit Tests

1. **ProductEventListenerTest**:

   - Test `handleProductDeleted` marks items as unavailable
   - Verify cache invalidation is called
   - Test error handling

2. **CartItemViewRepositoryTest**:

   - Test `markProductAsUnavailable` updates correct records
   - Test `deleteUnavailableProductItems` removes only unavailable items

3. **ProductServiceImplTest**:
   - Test `deleteProduct` publishes ProductDeletedEvent
   - Verify event contains correct product data

### E2E Tests

**Test Scenario**: Product Deletion Cart Synchronization

```java
@Test
@DisplayName("Product deletion marks cart items as unavailable")
void testProductDeletionCartSync() {
    // 1. Create product
    // 2. Add to user's cart
    // 3. Verify available=true
    // 4. Admin deletes product
    // 5. Wait for event propagation (awaitility)
    // 6. Verify cart item shows available=false
    // 7. Verify cart item still exists (not deleted)
}
```

## Configuration

### Kafka Topics

**Topic**: `product-events`  
**Partitions**: 3  
**Replication Factor**: 1 (dev), 3 (prod)

### Consumer Configuration

**Consumer Group**: `cart-service`  
**Auto Offset Reset**: earliest  
**Enable Auto Commit**: true

## Monitoring and Observability

### Logs

```
INFO  - Handling ProductDeletedEvent for productId: 123
INFO  - Marked all CartItemViews as unavailable for deleted productId: 123
```

### Metrics to Track

1. **ProductDeletedEvent Processing Time**: Measure event handling latency
2. **Cart Items Marked Unavailable**: Count per event
3. **Event Processing Failures**: Track and alert on errors

### Alerts

- Alert if ProductDeletedEvent processing fails
- Alert if cart item update transaction fails
- Alert if Kafka consumer lag exceeds threshold

## Future Enhancements

### 1. Batch Cleanup Job

Create a scheduled job to remove unavailable items older than X days:

```java
@Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
public void cleanupUnavailableItems() {
    int daysThreshold = 7;
    cartItemViewRepository.deleteUnavailableItemsOlderThan(
        LocalDateTime.now().minusDays(daysThreshold)
    );
}
```

### 2. User Notifications

- Send email/notification when items become unavailable
- Show in-app notification on next login
- Display banner in cart view

### 3. Alternative Product Suggestions

When a product becomes unavailable, suggest similar products:

```java
public List<ProductDto> getSuggestedAlternatives(Long unavailableProductId) {
    // Find products in same category with similar price range
}
```

### 4. Wishlist Migration

Allow users to move unavailable items to wishlist:

```java
public void moveUnavailableToWishlist(String userId) {
    List<CartItemView> unavailable = cartItemViewRepository
        .findByUserIdAndAvailableFalse(userId);

    unavailable.forEach(item -> {
        wishlistService.addItem(userId, item.getProductId());
        cartCommandService.removeItem(userId, item.getCartItemId());
    });
}
```

## Rollback Plan

If issues occur:

1. **Disable Event Publishing**: Comment out `publishProductDeletedEvent` call
2. **Rollback Migration**: Run reverse migration to remove `available` column
3. **Restart Services**: Restart cart service with old code
4. **Kafka Consumer Lag**: Monitor and address any lag in event processing

## Conclusion

This implementation provides a robust, user-friendly approach to handling product deletions across the microservices architecture. By marking items as unavailable rather than silently removing them, we maintain transparency and give users control over their shopping experience.

The event-driven design ensures loose coupling between services while maintaining data consistency. The addition of the `available` flag provides flexibility for future enhancements like batch cleanup, notifications, and wishlist migration.
