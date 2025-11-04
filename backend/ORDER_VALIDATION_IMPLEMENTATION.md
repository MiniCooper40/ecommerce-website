# Async Order Validation Implementation

## Overview

This document describes the implementation of asynchronous order validation using event-driven architecture. When an order is created, it triggers validation requests to both the cart service and catalog service to verify cart contents and product stock availability.

## Architecture

### Event Flow

```
Order Service (Order Creation)
    ↓
    Publishes: CartValidationRequestedEvent
    Publishes: ProductValidationRequestedEvent
    ↓
Cart Service ← (Listens for CartValidationRequestedEvent)
    ↓
    Validates: Cart exists, products available, quantities match
    ↓
    Publishes: CartValidationCompletedEvent
    ↓
Catalog Service ← (Listens for ProductValidationRequestedEvent)
    ↓
    Validates: Products exist, sufficient stock available
    ↓
    Publishes: ProductValidationCompletedEvent
    ↓
Order Service ← (Listens for both validation events)
    ↓
    Updates order validation state
    ↓
    If both validations pass: Order status → CONFIRMED
    If any validation fails: Order status → CANCELLED
```

## Changes Made

### 1. Order Entity (order-service)

**File**: `backend/services/order-service/src/main/java/com/ecommerce/order/entity/Order.java`

Added validation state fields:

- `cartValidated` (Boolean) - Tracks if cart validation completed successfully
- `stockValidated` (Boolean) - Tracks if stock validation completed successfully
- `validationCompletedAt` (LocalDateTime) - Timestamp when all validations completed

```java
@Column(name = "cart_validated")
private Boolean cartValidated = false;

@Column(name = "stock_validated")
private Boolean stockValidated = false;

@Column(name = "validation_completed_at")
private LocalDateTime validationCompletedAt;
```

### 2. Order Repository (order-service)

**File**: `backend/services/order-service/src/main/java/com/ecommerce/order/repository/OrderRepository.java`

Added method to find most recent pending order by user:

```java
Optional<Order> findFirstByUserIdAndStatusOrderByCreatedAtDesc(String userId, OrderStatus status);
```

### 3. Order Validation Event Handler (order-service)

**File**: `backend/services/order-service/src/main/java/com/ecommerce/order/listener/OrderValidationEventHandler.java`

**New Component** that listens for validation completion events:

#### Cart Validation Handler

- Listens to: `cart-events` topic
- Consumes: `CartValidationCompletedEvent`
- Updates: `order.cartValidated` field
- If validation fails: Sets order status to CANCELLED

#### Product Validation Handler

- Listens to: `product-events` topic
- Consumes: `ProductValidationCompletedEvent`
- Updates: `order.stockValidated` field
- If validation fails: Sets order status to CANCELLED

#### Validation Completion Logic

When both validations complete successfully:

- Sets order status to CONFIRMED
- Sets `validationCompletedAt` timestamp

### 4. Order Service Implementation (order-service)

**File**: `backend/services/order-service/src/main/java/com/ecommerce/order/service/OrderServiceImpl.java`

Modified `createOrder()` to publish validation requests after order creation:

```java
private void publishValidationRequests(Order order, CreateOrderRequest request) {
    // Publish CartValidationRequestedEvent
    // Publish ProductValidationRequestedEvent
}
```

Events are published asynchronously and don't block order creation.

### 5. Cart Validation Event Handler (cart-service)

**File**: `backend/services/cart-service/src/main/java/com/ecommerce/cart/listener/CartValidationEventHandler.java`

**New Component** that validates cart contents:

Validates:

1. Cart exists and is not empty
2. All requested products are in the cart
3. Products are still active/available
4. Quantities match the order request

Publishes: `CartValidationCompletedEvent` with validation result and any errors

### 6. Product Validation Handler (catalog-service)

**File**: `backend/services/catalog-service/src/main/java/com/ecommerce/catalog/events/ProductEventHandler.java`

**Already Implemented** - validates:

1. Products exist
2. Products are available
3. Sufficient stock quantity available

Publishes: `ProductValidationCompletedEvent` with validation result

### 7. Order DTO (order-service)

**File**: `backend/services/order-service/src/main/java/com/ecommerce/order/dto/OrderDto.java`

Added validation fields to response DTO:

```java
private Boolean cartValidated;
private Boolean stockValidated;
private LocalDateTime validationCompletedAt;
```

## Event Specifications

### CartValidationRequestedEvent

Published by: Order Service  
Topic: `cart-events`

```java
{
    "cartId": "cart-{userId}",
    "userId": "user123",
    "items": [
        {"productId": "1", "quantity": 2},
        {"productId": "2", "quantity": 1}
    ],
    "requestingService": "order-service",
    "correlationId": "{orderId}"
}
```

### CartValidationCompletedEvent

Published by: Cart Service  
Topic: `cart-events`

```java
{
    "cartId": "cart-{userId}",
    "userId": "user123",
    "isValid": true,
    "validationErrors": [],
    "requestingService": "order-service",
    "correlationId": "{orderId}"
}
```

### ProductValidationRequestedEvent

Published by: Order Service  
Topic: `product-events`

```java
{
    "requestId": "{orderId}",
    "productIds": ["1", "2"],
    "requiredQuantities": [
        {"productId": "1", "requiredQuantity": 2},
        {"productId": "2", "requiredQuantity": 1}
    ],
    "requestingService": "order-service",
    "correlationId": "{orderId}"
}
```

### ProductValidationCompletedEvent

Published by: Catalog Service  
Topic: `product-events`

```java
{
    "requestId": "{orderId}",
    "validProducts": ["1", "2"],
    "invalidProducts": [],
    "unavailableProducts": [],
    "isValid": true,
    "requestingService": "order-service",
    "correlationId": "{orderId}"
}
```

## Order Status Flow

1. **PENDING** - Order created, validation requests sent
2. **CONFIRMED** - All validations passed (cartValidated=true, stockValidated=true)
3. **CANCELLED** - Any validation failed

## Testing the Implementation

### 1. Create an Order

```bash
POST /api/orders
Authorization: Bearer {jwt-token}

{
    "shippingAddress": { ... },
    "billingAddress": { ... },
    "items": [
        {
            "productId": 1,
            "quantity": 2
        }
    ]
}
```

Response (initial state):

```json
{
  "id": 123,
  "status": "PENDING",
  "cartValidated": false,
  "stockValidated": false,
  "validationCompletedAt": null
}
```

### 2. Check Order After Validation

Wait a few seconds for async validation, then:

```bash
GET /api/orders/123
Authorization: Bearer {jwt-token}
```

Response (successful validation):

```json
{
  "id": 123,
  "status": "CONFIRMED",
  "cartValidated": true,
  "stockValidated": true,
  "validationCompletedAt": "2025-11-04T10:30:45"
}
```

### 3. Monitor Kafka Events

Check Kafka topics for event flow:

```bash
# Watch cart-events topic
kafka-console-consumer --bootstrap-server localhost:9092 --topic cart-events --from-beginning

# Watch product-events topic
kafka-console-consumer --bootstrap-server localhost:9092 --topic product-events --from-beginning
```

### 4. Test Validation Failures

#### Out of Stock Scenario

- Create order with quantity exceeding available stock
- Product validation should fail
- Order status should be CANCELLED

#### Invalid Cart Scenario

- Create order with products not in user's cart
- Cart validation should fail
- Order status should be CANCELLED

## Error Handling

### Event Processing Failures

- If event processing fails, messages are NOT acknowledged
- Kafka will retry based on consumer configuration
- Errors are logged with correlation IDs for tracing

### Validation Timeouts

- Currently no timeout mechanism implemented
- Orders could remain in PENDING state indefinitely if validation events are lost
- **Future Enhancement**: Add timeout mechanism to auto-cancel stale pending orders

## Configuration

All services use the same Kafka configuration from `application.yml`:

```yaml
ecommerce:
  events:
    bootstrap-servers: localhost:9092
    topics:
      product-events: product-events
      cart-events: cart-events
      order-events: order-events
```

## Database Migration

New columns added to `orders` table:

```sql
ALTER TABLE orders ADD COLUMN cart_validated BOOLEAN DEFAULT false;
ALTER TABLE orders ADD COLUMN stock_validated BOOLEAN DEFAULT false;
ALTER TABLE orders ADD COLUMN validation_completed_at TIMESTAMP;
```

These will be auto-created by Hibernate with `ddl-auto: update` setting.

## Known Limitations

1. **Cart ID Assumption**: Currently uses synthetic cart ID `cart-{userId}`. In production, would use actual cart IDs.

2. **Correlation by User**: Cart validation finds the most recent pending order for a user. Better approach would pass order ID in validation request.

3. **No Timeout**: Orders could remain in PENDING state forever if validation events are lost.

4. **No Compensation**: If validation fails, no compensation logic (e.g., restore cart, notify user).

## Future Enhancements

1. **Add Timeout Mechanism**: Scheduled job to cancel orders stuck in PENDING for > X minutes

2. **Add Saga Coordinator**: Centralized saga orchestrator to manage the validation workflow

3. **Add Compensation Logic**: When validation fails, trigger compensation events

4. **Add Order Events**: Publish `OrderConfirmedEvent` and `OrderCancelledEvent` for downstream consumers

5. **Add Retry Configuration**: Configure Kafka consumer retry policies and dead letter queues

6. **Add Idempotency**: Ensure validation handlers are idempotent to handle duplicate events

7. **Add Monitoring**: Add metrics for validation success rate, processing time, failures

## Related Files

- `backend/shared/events-lib/` - Shared event definitions and publisher
- `backend/services/order-service/` - Order service implementation
- `backend/services/cart-service/` - Cart service with validation handler
- `backend/services/catalog-service/` - Catalog service with validation handler

## References

- [Events Library README](../shared/events-lib/README.md)
- [Catalog Kafka Events Implementation](../services/catalog-service/KAFKA_EVENTS_IMPLEMENTATION.md)
- [Cart Event Listener README](../services/cart-service/CART_EVENT_LISTENER_README.md)
