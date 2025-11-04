# Order Validation Testing Guide

## Quick Start

This guide provides step-by-step instructions for testing the async order validation implementation.

## Prerequisites

1. All services running (Order, Cart, Catalog, Security, Eureka, Gateway)
2. Kafka running on localhost:9092
3. PostgreSQL databases for all services
4. Valid JWT token

## Test Scenarios

### Scenario 1: Successful Order Validation

**Objective**: Create order, verify both cart and stock validations pass, order confirms

#### Steps:

1. **Login and get JWT token**

```bash
POST http://localhost:8080/api/auth/login
{
    "username": "testuser",
    "password": "password"
}
```

Save the token from response.

2. **Add products to cart** (optional - depends on cart validation logic)

```bash
POST http://localhost:8080/api/cart/items
Authorization: Bearer {token}
{
    "productId": 1,
    "quantity": 2
}
```

3. **Create an order**

```bash
POST http://localhost:8080/api/orders
Authorization: Bearer {token}
{
    "shippingAddress": {
        "street": "123 Main St",
        "city": "Test City",
        "state": "TS",
        "zipCode": "12345",
        "country": "Test Country"
    },
    "billingAddress": {
        "street": "123 Main St",
        "city": "Test City",
        "state": "TS",
        "zipCode": "12345",
        "country": "Test Country"
    },
    "items": [
        {
            "productId": 1,
            "quantity": 2
        }
    ]
}
```

4. **Check order status immediately**

```bash
GET http://localhost:8080/api/orders/{orderId}
Authorization: Bearer {token}
```

Expected response:

```json
{
    "id": 1,
    "status": "PENDING",
    "cartValidated": false,
    "stockValidated": false,
    "validationCompletedAt": null,
    ...
}
```

5. **Wait 2-3 seconds, then check order again**

Expected response:

```json
{
    "id": 1,
    "status": "CONFIRMED",
    "cartValidated": true,
    "stockValidated": true,
    "validationCompletedAt": "2025-11-04T10:30:45.123",
    ...
}
```

### Scenario 2: Stock Validation Failure

**Objective**: Create order exceeding available stock, verify order is cancelled

#### Steps:

1. **Create product with limited stock**

```bash
POST http://localhost:8080/api/products
Authorization: Bearer {admin-token}
{
    "name": "Limited Stock Product",
    "price": 99.99,
    "stockQuantity": 5,
    "category": "Test",
    "active": true
}
```

Note the product ID.

2. **Create order with excessive quantity**

```bash
POST http://localhost:8080/api/orders
Authorization: Bearer {token}
{
    "shippingAddress": { ... },
    "billingAddress": { ... },
    "items": [
        {
            "productId": {product-id},
            "quantity": 10  // More than available stock (5)
        }
    ]
}
```

3. **Wait 2-3 seconds, check order status**

Expected response:

```json
{
    "id": 2,
    "status": "CANCELLED",
    "cartValidated": true,  // May or may not complete
    "stockValidated": false,
    "validationCompletedAt": "2025-11-04T10:32:15.456",
    ...
}
```

### Scenario 3: Cart Validation Failure

**Objective**: Create order with products not in cart

#### Steps:

1. **Create order without adding products to cart first**

```bash
POST http://localhost:8080/api/orders
Authorization: Bearer {token}
{
    "shippingAddress": { ... },
    "billingAddress": { ... },
    "items": [
        {
            "productId": 999,  // Product not in cart
            "quantity": 1
        }
    ]
}
```

2. **Wait 2-3 seconds, check order status**

Expected response:

```json
{
    "id": 3,
    "status": "CANCELLED",
    "cartValidated": false,
    "stockValidated": true,  // May or may not complete
    "validationCompletedAt": "2025-11-04T10:33:20.789",
    ...
}
```

## Monitoring Events

### View Kafka Events

**Terminal 1 - Cart Events**

```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic cart-events \
    --from-beginning \
    --property print.key=true \
    --property print.timestamp=true
```

**Terminal 2 - Product Events**

```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic product-events \
    --from-beginning \
    --property print.key=true \
    --property print.timestamp=true
```

### Expected Event Sequence

For a successful order:

```
1. CartValidationRequestedEvent (cart-events)
2. ProductValidationRequestedEvent (product-events)
3. CartValidationCompletedEvent (cart-events) - isValid: true
4. ProductValidationCompletedEvent (product-events) - isValid: true
```

## Service Logs

### Order Service Logs

Check for:

```
Published CartValidationRequestedEvent for order: {orderId}
Published ProductValidationRequestedEvent for order: {orderId}
Received CartValidationCompletedEvent for cartId: ...
Received ProductValidationCompletedEvent for requestId: ...
All validations completed successfully for order: {orderId}
```

### Cart Service Logs

Check for:

```
Received CartValidationRequestedEvent for cartId: ...
Published CartValidationCompletedEvent for cartId: ..., valid: true
```

### Catalog Service Logs

Check for:

```
Received product validation request for products: [1, 2] from service: order-service
Sent product validation response for request: {orderId} - Valid: true
```

## Database Verification

### Check Order Status in Database

```sql
-- Connect to order_db
SELECT id, user_id, status, cart_validated, stock_validated,
       validation_completed_at, created_at
FROM orders
ORDER BY created_at DESC
LIMIT 10;
```

Expected columns:

- `cart_validated`: true/false
- `stock_validated`: true/false
- `validation_completed_at`: timestamp (null if pending)
- `status`: PENDING, CONFIRMED, or CANCELLED

## Troubleshooting

### Order Stuck in PENDING Status

**Possible Causes:**

1. Kafka not running
2. Event listeners not registered
3. Event publishing failed
4. Database transaction failed

**Debug Steps:**

1. Check Kafka topics exist: `kafka-topics --list --bootstrap-server localhost:9092`
2. Check service logs for errors
3. Verify event was published (check Kafka consumer output)
4. Check database for partial updates

### Validation Always Fails

**Possible Causes:**

1. Cart validation logic too strict
2. Products not properly set up
3. User cart empty

**Debug Steps:**

1. Check cart-service logs for validation errors
2. Verify products exist and are active
3. Check cart contents for user

### Events Not Being Consumed

**Possible Causes:**

1. Consumer group offset issues
2. Kafka connection problems
3. Listener not registered

**Debug Steps:**

1. Check Kafka consumer groups: `kafka-consumer-groups --bootstrap-server localhost:9092 --list`
2. Check consumer lag: `kafka-consumer-groups --bootstrap-server localhost:9092 --group order-service-validation --describe`
3. Restart services to re-register listeners

## Performance Testing

### Load Test Order Creation

```bash
# Using Apache Bench
ab -n 100 -c 10 -H "Authorization: Bearer {token}" \
   -p order.json -T application/json \
   http://localhost:8080/api/orders

# Using curl in loop
for i in {1..10}; do
    curl -X POST http://localhost:8080/api/orders \
         -H "Authorization: Bearer {token}" \
         -H "Content-Type: application/json" \
         -d @order.json &
done
wait
```

### Monitor Validation Processing Time

Check logs for timestamps:

- Order creation timestamp
- Validation request published
- Validation response received
- Order status updated

Expected total time: < 1 second for successful validations

## Success Criteria

✅ Order created with status PENDING  
✅ CartValidationRequestedEvent published  
✅ ProductValidationRequestedEvent published  
✅ Cart service processes validation request  
✅ Catalog service processes validation request  
✅ CartValidationCompletedEvent published  
✅ ProductValidationCompletedEvent published  
✅ Order service receives both validation events  
✅ Order status updated to CONFIRMED (or CANCELLED if validation fails)  
✅ validation_completed_at timestamp set  
✅ Total process completes in < 2 seconds

## Additional Resources

- [Order Validation Implementation](./ORDER_VALIDATION_IMPLEMENTATION.md)
- [Events Library README](../shared/events-lib/README.md)
- [Kafka Events Implementation](../services/catalog-service/KAFKA_EVENTS_IMPLEMENTATION.md)
