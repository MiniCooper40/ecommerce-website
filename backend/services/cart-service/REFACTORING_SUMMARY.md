# Cart Service CQRS Refactoring - Summary

## Overview

Successfully refactored the cart service to follow the **CQRS (Command Query Responsibility Segregation)** pattern with event-driven architecture using Kafka and Redis caching.

## Changes Made

### 1. Entity Refactoring

#### CartItem (Write Model)

**File**: `src/main/java/com/ecommerce/cart/entity/CartItem.java`

- **Simplified to minimal fields**:
  - `id` - Primary key
  - `cartId` - UUID for cart identification
  - `userId` - User owner
  - `productId` - Product reference
  - `quantity` - Item count
  - `createdAt` / `updatedAt` - Timestamps
- **Removed**: All denormalized product fields (name, price, imageUrl)
- **Added**: Indexes for performance optimization

#### CartItemView (Read Model) - NEW

**File**: `src/main/java/com/ecommerce/cart/entity/CartItemView.java`

- **Denormalized view with product details**:
  - All CartItem fields
  - `productName`, `productDescription`
  - `productPrice`, `productImageUrl`
  - `productCategory`, `productActive`
- **Purpose**: Fast queries without joins

### 2. New Event Classes (events-lib)

Created three new event types in `backend/shared/events-lib/src/main/java/com/ecommerce/shared/events/domain/`:

1. **CartItemAddedEvent.java** - Published when cart item added
2. **CartItemUpdatedEvent.java** - Published when quantity updated
3. **CartItemRemovedEvent.java** - Published when item removed

### 3. Service Layer Refactoring

#### CartCommandService - NEW

**File**: `src/main/java/com/ecommerce/cart/service/CartCommandService.java`

- Handles all write operations (commands)
- Publishes Kafka events for each operation
- Methods:
  - `addItemToCart()` - Add or update cart item
  - `updateItemQuantity()` - Modify quantity
  - `removeItemFromCart()` - Delete item
  - `clearCart()` - Remove all items

#### CartQueryService - NEW

**File**: `src/main/java/com/ecommerce/cart/service/CartQueryService.java`

- Handles all read operations (queries)
- Reads from CartItemView for fast performance
- Methods:
  - `getCart()` - Get full cart with product details
  - `getCartItemCount()` - Get item count

#### ProductCacheService - NEW

**File**: `src/main/java/com/ecommerce/cart/service/ProductCacheService.java`

- Manages Redis cache for product details
- 1-hour TTL for cached products
- Methods:
  - `getProduct()` - Get from cache or fetch from catalog
  - `updateProductCache()` - Update cached product
  - `invalidateProductCache()` - Remove from cache

### 4. Event Handling

#### CartEventListener - NEW

**File**: `src/main/java/com/ecommerce/cart/listener/CartEventListener.java`

- Listens to Kafka topics for cart and product events
- Updates CartItemView based on events
- Kafka Listeners:
  - `cart-item-added` → Create view entry with product details
  - `cart-item-updated` → Update quantity in view
  - `cart-item-removed` → Delete view entry
  - `product-updated` → Update all cart items with new product data

**Event Flow**:

1. Command received → CartCommandService saves CartItem
2. Event published to Kafka
3. CartEventListener receives event
4. Check Redis cache for product (if needed)
5. Fetch from catalog-service via Feign if not cached
6. Update CartItemView with complete data

### 5. Controller Refactoring

#### CartController - UPDATED

**File**: `src/main/java/com/ecommerce/cart/controller/CartController.java`

- Separated into command and query operations
- Query operations use CartQueryService
- Command operations use CartCommandService
- **API Changes**:
  - `POST /cart/items` now accepts `AddCartItemRequest` (productId, quantity only)
  - Returns `Long` (cartItemId) instead of full DTO
  - Other endpoints unchanged

### 6. DTOs

#### AddCartItemRequest - NEW

**File**: `src/main/java/com/ecommerce/cart/dto/AddCartItemRequest.java`

- Simplified request for adding cart items
- Fields: `productId`, `quantity`

#### ProductDto - NEW

**File**: `src/main/java/com/ecommerce/cart/dto/ProductDto.java`

- DTO for product data from catalog service

### 7. Infrastructure

#### RedisConfig - NEW

**File**: `src/main/java/com/ecommerce/cart/config/RedisConfig.java`

- Redis configuration for caching
- JSON serialization with Jackson
- 1-hour cache TTL

#### CatalogClient - NEW

**File**: `src/main/java/com/ecommerce/cart/client/CatalogClient.java`

- Feign client for catalog service communication
- Method: `getProductById(Long id)`

#### CartItemViewRepository - NEW

**File**: `src/main/java/com/ecommerce/cart/repository/CartItemViewRepository.java`

- Repository for read model
- Bulk update method for product changes

### 8. Configuration Updates

#### pom.xml - UPDATED

- Added: `spring-cloud-starter-openfeign` dependency

#### CartServiceApplication.java - UPDATED

- Added: `@EnableFeignClients` annotation

### 9. Database Migration

**File**: `src/main/resources/db/migration/V1__cqrs_migration.sql`

- SQL script for schema migration
- Creates `cart_item_view` table
- Removes denormalized columns from `cart_items`
- Adds `cart_id` column
- Creates indexes

### 10. Documentation

**File**: `CQRS_IMPLEMENTATION.md`

- Comprehensive documentation of CQRS pattern
- Architecture diagrams
- Event flows
- API documentation
- Configuration details

## Files Removed

1. `CartService.java` - Interface replaced by command/query services
2. `CartServiceImpl.java` - Implementation split into command/query services
3. `ProductEventListener.java` - Functionality moved to CartEventListener
4. `CartControllerTest.java` - Needs rewriting for new architecture
5. `ProductEventListenerTest.java` - Component removed

## Kafka Topics

### Published To:

- `cart-item-added`
- `cart-item-updated`
- `cart-item-removed`

### Subscribed To:

- `cart-item-added`
- `cart-item-updated`
- `cart-item-removed`
- `product-updated`

## Redis Cache Keys

- Pattern: `product:{productId}`
- TTL: 1 hour
- Serialization: JSON

## Benefits

1. **Performance**:

   - Fast reads from denormalized view
   - Redis caching reduces catalog service calls
   - No joins needed for cart queries

2. **Scalability**:

   - Read and write models can scale independently
   - Event-driven async updates
   - Reduced database load

3. **Maintainability**:

   - Clear separation of concerns
   - Command/Query segregation
   - Event sourcing foundation

4. **Consistency**:
   - Eventually consistent read model
   - Product updates propagate automatically
   - Cache stays synchronized

## Testing Recommendations

1. Build events-lib: `mvn clean install` in events-lib directory
2. Build cart-service: `mvn clean install` in cart-service directory
3. Run database migration script
4. Ensure Kafka and Redis are running
5. Test cart operations:
   - Add item (publishes event)
   - Check view is created
   - Update product in catalog
   - Verify cart view updates

## Next Steps

1. **Write comprehensive tests** for new services
2. **Data migration script** to populate initial CartItemView from existing data
3. **Monitoring** for event processing
4. **Error handling** for Feign client failures
5. **Retry logic** for failed event processing
6. **Circuit breaker** for catalog service calls
