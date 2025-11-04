# Cart Service CQRS Deployment Checklist

## Pre-Deployment

### 1. Build Verification

- [x] Events library built successfully (`mvn clean install`)
- [x] Cart service built successfully (`mvn clean install`)
- [ ] All services can discover each other via Eureka
- [ ] Kafka is running and accessible
- [ ] Redis is running and accessible
- [ ] PostgreSQL is running with cart_db

### 2. Database Migration

- [ ] Backup existing cart_items table
- [ ] Run migration script: `V1__cqrs_migration.sql`
- [ ] Verify cart_item_view table created
- [ ] Verify indexes created
- [ ] Verify cart_id column added to cart_items

### 3. Configuration Verification

#### application.yml

```yaml
# Verify these configurations exist:
spring:
  data:
    redis:
      host: localhost # or redis for Docker
      port: 6379

ecommerce:
  events:
    bootstrap-servers: localhost:9092 # or kafka:9093 for Docker
```

### 4. Service Dependencies

- [ ] Eureka server running (port 8761)
- [ ] Catalog service running and registered with Eureka
- [ ] Security service running (for JWT validation)
- [ ] Kafka broker running (port 9092 or 9093)
- [ ] Redis server running (port 6379)
- [ ] PostgreSQL running (port 5434 for local, 5432 for Docker)

### 5. Kafka Topics

Create these topics if they don't exist:

```bash
# Local Kafka
kafka-topics.sh --create --topic cart-item-added --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic cart-item-updated --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic cart-item-removed --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Verify topics
kafka-topics.sh --list --bootstrap-server localhost:9092
```

## Deployment Steps

### Step 1: Deploy Events Library

```bash
cd backend/shared/events-lib
mvn clean install
```

### Step 2: Deploy Cart Service

```bash
cd backend/services/cart-service
mvn clean package
java -jar target/cart-service-1.0.0-SNAPSHOT.jar
```

Or with Docker:

```bash
cd backend/services/cart-service
docker build -t cart-service:latest .
docker run -p 8084:8084 cart-service:latest
```

### Step 3: Verify Service Startup

Check logs for:

- [x] ✅ Spring Boot started successfully
- [x] ✅ Connected to Eureka
- [x] ✅ Connected to PostgreSQL
- [x] ✅ Connected to Redis
- [x] ✅ Connected to Kafka
- [x] ✅ Feign clients registered
- [x] ✅ Kafka listeners registered

### Step 4: Health Check

```bash
# Check service health
curl http://localhost:8084/actuator/health

# Expected response:
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" },
    "kafka": { "status": "UP" }
  }
}
```

## Post-Deployment Testing

### 1. Basic Cart Operations

#### Test 1: Add Item to Cart

```bash
# Get JWT token first
TOKEN="<your-jwt-token>"

# Add item
curl -X POST http://localhost:8084/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'

# Expected: Returns cart item ID (e.g., 123)
```

#### Test 2: Verify Event Processing

```bash
# Wait 500ms for event processing

# Get cart
curl http://localhost:8084/cart \
  -H "Authorization: Bearer $TOKEN"

# Expected: Cart item with full product details
{
  "items": [
    {
      "id": 123,
      "productId": 1,
      "productName": "Product Name",
      "productPrice": 29.99,
      "productImageUrl": "...",
      "quantity": 2
    }
  ]
}
```

#### Test 3: Check Database

```sql
-- Verify write model
SELECT * FROM cart_items;
-- Should have: id, cart_id, user_id, product_id, quantity

-- Verify read model
SELECT * FROM cart_item_view;
-- Should have: all cart fields + product details
```

#### Test 4: Check Redis Cache

```bash
redis-cli

# Check for cached product
GET "product:1"

# Should return JSON product data
```

#### Test 5: Check Kafka Events

```bash
# Consume events
kafka-console-consumer.sh --topic cart-item-added \
  --from-beginning --bootstrap-server localhost:9092

# Should see CartItemAddedEvent
```

### 2. Event Processing Tests

#### Test 1: Product Update Propagation

```bash
# Update product in catalog service
curl -X PUT http://localhost:8082/products/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product Name",
    "price": 39.99,
    "imageUrl": "new-image.jpg"
  }'

# Wait for event processing

# Check cart view updated
curl http://localhost:8084/cart \
  -H "Authorization: Bearer $TOKEN"

# Product details in cart should be updated
```

#### Test 2: Multiple Operations

```bash
# Add multiple items
for i in {1..5}; do
  curl -X POST http://localhost:8084/cart/items \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"productId\": $i, \"quantity\": 1}"
done

# Verify all items in cart with details
curl http://localhost:8084/cart \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Error Handling Tests

#### Test 1: Catalog Service Down

```bash
# Stop catalog service
# Add item - should still create cart item
curl -X POST http://localhost:8084/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 999, "quantity": 1}'

# Cart item created but view may not have product details
```

#### Test 2: Redis Down

```bash
# Stop Redis
# Add item - should fetch from catalog (slower)
curl -X POST http://localhost:8084/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 1}'

# Should still work, just slower
```

## Monitoring

### Key Metrics to Monitor

1. **Event Processing Lag**: Kafka consumer lag
2. **Cache Hit Ratio**: Redis cache hits vs misses
3. **Database Performance**: Query times for cart_item_view
4. **Feign Client Errors**: Catalog service call failures
5. **Event Processing Errors**: Failed event handlers

### Logging

Check these log patterns:

```
INFO  - Published CartItemAddedEvent for cartItemId: 123
INFO  - Handling CartItemAddedEvent: 123
INFO  - Product found in cache: 1
INFO  - Created CartItemView for cartItemId: 123
```

## Rollback Plan

If issues occur:

### 1. Database Rollback

```sql
-- Restore from backup
DROP TABLE cart_item_view;
-- Restore cart_items from cart_items_backup
-- Re-add denormalized columns
```

### 2. Service Rollback

- Deploy previous cart service version
- Stop publishing new events
- Drain Kafka topics

### 3. Data Consistency Check

```sql
-- Verify write model
SELECT COUNT(*) FROM cart_items;

-- Verify read model
SELECT COUNT(*) FROM cart_item_view;

-- Should be equal
```

## Success Criteria

- [x] ✅ Cart service starts without errors
- [ ] ✅ All dependencies connected (DB, Redis, Kafka, Eureka)
- [ ] ✅ Can add items to cart
- [ ] ✅ Cart view populated with product details
- [ ] ✅ Events published and consumed successfully
- [ ] ✅ Product updates reflect in cart
- [ ] ✅ Cache working (check Redis)
- [ ] ✅ No errors in logs
- [ ] ✅ Response times < 500ms for cart queries

## Common Issues

### Issue 1: View not populated after adding item

**Solution**: Check Kafka consumer group lag, verify catalog service is running

### Issue 2: Cache misses

**Solution**: Verify Redis connection, check TTL configuration

### Issue 3: Feign client errors

**Solution**: Verify catalog service registered in Eureka, check service name

### Issue 4: Database errors

**Solution**: Verify migration script ran, check table schemas

## Support Contacts

- **Backend Team**: For service issues
- **DevOps**: For infrastructure (Kafka, Redis, PostgreSQL)
- **Frontend Team**: For API integration issues

## Documentation

- [CQRS_IMPLEMENTATION.md](./CQRS_IMPLEMENTATION.md) - Architecture details
- [API_MIGRATION_GUIDE.md](./API_MIGRATION_GUIDE.md) - Frontend migration
- [REFACTORING_SUMMARY.md](./REFACTORING_SUMMARY.md) - Change summary
