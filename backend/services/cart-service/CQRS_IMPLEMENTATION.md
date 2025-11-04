# Cart Service CQRS Implementation

## Overview

The cart service has been refactored to follow the **CQRS (Command Query Responsibility Segregation)** pattern. This separates read and write operations, using different data models optimized for each type of operation.

## Architecture

### Write Model (Command Side)

- **Entity**: `CartItem` - Minimal entity with only essential data
- **Repository**: `CartItemRepository`
- **Service**: `CartCommandService`
- **Fields**:
  - `id` - Primary key
  - `cartId` - Unique cart identifier (UUID)
  - `userId` - User who owns the cart
  - `productId` - Reference to product
  - `quantity` - Number of items
  - `createdAt` / `updatedAt` - Timestamps

### Read Model (Query Side)

- **Entity**: `CartItemView` - Denormalized entity with product details
- **Repository**: `CartItemViewRepository`
- **Service**: `CartQueryService`
- **Fields**:
  - All fields from `CartItem`
  - `productName` - Cached product name
  - `productDescription` - Cached product description
  - `productPrice` - Cached product price
  - `productImageUrl` - Cached product image
  - `productCategory` - Cached product category
  - `productActive` - Product availability status

## Event-Driven Architecture

### Events Published (Command Side)

1. **CartItemAddedEvent** - When a new item is added
2. **CartItemUpdatedEvent** - When quantity is updated
3. **CartItemRemovedEvent** - When an item is removed

### Events Consumed (Query Side)

1. **CartItemAddedEvent** - Triggers product fetch and view creation
2. **CartItemUpdatedEvent** - Updates quantity in view
3. **CartItemRemovedEvent** - Deletes view entry
4. **ProductUpdatedEvent** - Updates all cart items with new product details

## Product Caching Strategy

### Redis Cache

- **Service**: `ProductCacheService`
- **TTL**: 1 hour
- **Key Pattern**: `product:{productId}`

### Cache Flow

1. When a cart item is added, check Redis cache for product details
2. If not in cache, fetch from catalog service via Feign client
3. Cache the product details in Redis
4. Use cached data to populate `CartItemView`

### Cache Invalidation

- Product cache is updated when `ProductUpdatedEvent` is received
- Ensures cart always shows current product information

## API Endpoints

### Query Operations (Read)

```
GET /cart - Get user's cart with full product details
GET /cart/count - Get total item count in cart
```

### Command Operations (Write)

```
POST /cart/items - Add item to cart
  Request: { "productId": 123, "quantity": 2 }
  Response: cartItemId (Long)

PUT /cart/items/{itemId}/quantity?quantity=3 - Update item quantity
DELETE /cart/items/{itemId} - Remove item from cart
DELETE /cart - Clear entire cart
```

## Data Flow

### Adding a Cart Item

```
1. Client → POST /cart/items
2. CartController → CartCommandService.addItemToCart()
3. Save minimal CartItem to database
4. Publish CartItemAddedEvent to Kafka
5. CartEventListener receives event
6. ProductCacheService checks Redis cache
7. If not cached, CatalogClient fetches from catalog-service
8. Create CartItemView with product details
9. Client queries GET /cart
10. CartQueryService returns data from CartItemView
```

### Updating Product Information

```
1. Catalog service updates product
2. Publishes ProductUpdatedEvent to Kafka
3. CartEventListener receives event
4. Updates Redis cache with new product data
5. Updates all CartItemView records for that product
6. Users see updated product info in their carts
```

## Benefits

### Performance

- **Reads are fast**: Query from denormalized view, no joins needed
- **Caching**: Redis reduces calls to catalog service
- **Scalability**: Read and write databases can be scaled independently

### Maintainability

- **Separation of concerns**: Command and query logic are separate
- **Event-driven**: Loose coupling between services
- **Audit trail**: Events provide complete history of cart operations

### Consistency

- **Eventually consistent**: Read model is updated asynchronously
- **Product sync**: Automatic updates when products change
- **Cache coherence**: Redis cache stays in sync with product updates

## Database Migration

Run the migration script to update the schema:

```sql
-- Located at: src/main/resources/db/migration/V1__cqrs_migration.sql
```

This script:

1. Backs up existing cart_items data
2. Creates cart_item_view table
3. Removes denormalized columns from cart_items
4. Adds cart_id column
5. Creates appropriate indexes

## Configuration

### Kafka Topics

- `cart-item-added`
- `cart-item-updated`
- `cart-item-removed`
- `product-updated`

### Redis Configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Feign Client

```yaml
# Uses Eureka service discovery
# Client: CatalogClient → catalog-service
```

## Testing Considerations

1. **Command Tests**: Verify events are published correctly
2. **Query Tests**: Verify data is read from CartItemView
3. **Event Handler Tests**: Verify view is updated on events
4. **Cache Tests**: Verify Redis caching behavior
5. **Integration Tests**: End-to-end flow with Kafka and Redis

## Future Enhancements

1. **Event Sourcing**: Store all events for complete audit trail
2. **SAGA Pattern**: Coordinate multi-service transactions
3. **Read Replicas**: Separate read database for even better performance
4. **Cache Warming**: Pre-populate cache with frequently accessed products
5. **Batch Updates**: Optimize bulk product updates
