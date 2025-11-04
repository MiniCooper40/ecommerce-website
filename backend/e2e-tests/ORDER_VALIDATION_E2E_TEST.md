# Order Validation E2E Test

## Overview

This E2E test validates the complete asynchronous order validation flow, including:

1. **Product Creation** - Admin creates products with varying stock levels
2. **Cart Management** - User adds products to cart
3. **Invalid Order Attempt** - User tries to create order with insufficient stock
4. **Order Cancellation** - System validates and cancels order due to stock shortage
5. **Cart Correction** - User updates cart to valid quantities
6. **Valid Order Creation** - User creates order with valid stock levels
7. **Order Confirmation** - System validates and confirms order

## Test Flow

```
1. Admin creates Product 1 (Limited Stock: 5 units)
2. Admin creates Product 2 (Good Stock: 100 units)
3. User adds Product 1 to cart (10 units - EXCEEDS STOCK)
4. User adds Product 2 to cart (2 units - valid)
5. User creates Order 1 → Status: PENDING
   ↓
   CartValidationRequestedEvent → Cart Service
   ProductValidationRequestedEvent → Catalog Service
   ↓
   Cart Service validates → CartValidationCompletedEvent (valid)
   Catalog Service validates → ProductValidationCompletedEvent (INVALID - stock exceeded)
   ↓
6. Order 1 → Status: CANCELLED (stockValidated=false)
7. User updates cart: Product 1 quantity 10 → 3
8. User creates Order 2 → Status: PENDING
   ↓
   CartValidationRequestedEvent → Cart Service
   ProductValidationRequestedEvent → Catalog Service
   ↓
   Cart Service validates → CartValidationCompletedEvent (valid)
   Catalog Service validates → ProductValidationCompletedEvent (valid)
   ↓
9. Order 2 → Status: CONFIRMED (cartValidated=true, stockValidated=true)
```

## Prerequisites

### 1. Build All Services

Build Docker images for all services:

```bash
cd backend
./build-images.bat   # Windows
# or
./build-images.sh    # Linux/Mac
```

This builds images for:

- eureka-server
- security-service
- catalog-service
- cart-service
- order-service
- gateway

### 2. Verify Docker Images

Check that all images are built:

```bash
docker images | grep ecommerce
```

Expected output:

```
ecommerce/gateway           latest
ecommerce/order-service     latest
ecommerce/cart-service      latest
ecommerce/catalog-service   latest
ecommerce/security-service  latest
ecommerce/eureka-server     latest
```

## Running the Test

### Option 1: Run from Maven (Recommended)

```bash
cd backend/e2e-tests
mvn test -Dtest=OrderValidationE2ETest
```

### Option 2: Run from IDE

1. Open the E2E Tests workspace folder in VS Code
2. Navigate to: `src/test/java/com/ecommerce/e2e/tests/OrderValidationE2ETest.java`
3. Click "Run Test" above the class name

### Option 3: Run All E2E Tests

```bash
cd backend/e2e-tests
mvn verify
```

## Test Execution Details

### Infrastructure Startup

The test automatically starts the following containers via Testcontainers:

1. **PostgreSQL** - Shared database for all services
2. **Kafka + Zookeeper** - Event messaging
3. **Redis** - Cart caching
4. **Eureka Server** - Service discovery
5. **Security Service** - Authentication & authorization
6. **Catalog Service** - Product management
7. **Cart Service** - Shopping cart management
8. **Order Service** - Order processing
9. **Gateway** - API gateway

**Startup Time**: Approximately 2-3 minutes

### Test Duration

- **Total Duration**: ~5-7 minutes (including infrastructure startup)
- **Test Execution**: ~1-2 minutes (after infrastructure is ready)

### Async Validation Wait Times

The test uses Awaitility to wait for async validations:

- **Cart Validation**: Max 30 seconds (typically completes in 2-5 seconds)
- **Stock Validation**: Max 30 seconds (typically completes in 2-5 seconds)

## Expected Results

All 12 test steps should pass:

```
✓ TEST 1: Admin creates Product 1 with limited stock (5 units)
✓ TEST 2: Admin creates Product 2 with good stock (100 units)
✓ TEST 3: User adds Product 1 to cart (10 units - exceeds stock)
✓ TEST 4: User adds Product 2 to cart (2 units)
✓ TEST 5: User verifies cart has both products
✓ TEST 6: User creates order (will fail validation)
✓ TEST 7: Order CANCELLED due to insufficient stock
✓ TEST 8: User updates cart to valid quantity (3 units)
✓ TEST 9: User verifies updated cart
✓ TEST 10: User creates order with valid cart
✓ TEST 11: Order CONFIRMED after successful validation
✓ TEST 12: Both orders verified with correct states
```

## Validation Checks

The test verifies:

### Order 1 (Invalid - Cancelled)

- ✓ Created with status: PENDING
- ✓ cartValidated: false initially
- ✓ stockValidated: false initially
- ✓ Final status: CANCELLED
- ✓ stockValidated: false (stock exceeded)
- ✓ validationCompletedAt: not null

### Order 2 (Valid - Confirmed)

- ✓ Created with status: PENDING
- ✓ cartValidated: false initially
- ✓ stockValidated: false initially
- ✓ Final status: CONFIRMED
- ✓ cartValidated: true
- ✓ stockValidated: true
- ✓ validationCompletedAt: not null

## Troubleshooting

### Test Timeout

If tests timeout waiting for validation:

1. **Check Kafka Connectivity**

   ```bash
   docker logs <kafka-container-id>
   ```

2. **Check Service Logs**

   - Order Service logs should show validation events published
   - Cart Service logs should show validation processing
   - Catalog Service logs should show stock validation

3. **Verify Event Topics**
   The test uses:
   - `cart-events` topic
   - `product-events` topic

### Container Startup Issues

If containers fail to start:

1. **Check Docker Resources**

   - Ensure Docker has enough memory (recommended: 4GB+)
   - Ensure Docker has enough CPU (recommended: 2+ cores)

2. **Clear Docker State**

   ```bash
   docker system prune -a
   docker volume prune
   ```

3. **Rebuild Images**
   ```bash
   cd backend
   ./build-images.bat --no-cache
   ```

### Service Registration Issues

If services don't register with Eureka:

1. The test waits 30 seconds after startup for Eureka registration
2. Check Eureka logs for registration events
3. Increase wait time if needed (edit E2ETestBase.java)

## Viewing Test Output

### Detailed Logging

The test produces detailed logs showing:

- Infrastructure startup progress
- Service URLs and ports
- Test step execution
- Order validation status transitions
- Final order states

### Example Output

```
================================================================================
TEST 6: User attempts to create order (will fail stock validation)
================================================================================
✓ Order created with ID: 1 (Status: PENDING, awaiting validation)

================================================================================
TEST 7: Waiting for async validation to complete (should CANCEL)
================================================================================
Order 1 validation status - Status: PENDING, StockValidated: false
Order 1 validation status - Status: CANCELLED, StockValidated: false
✓ Order 1 correctly CANCELLED due to insufficient stock
```

## Clean Up

Testcontainers automatically cleans up containers after tests complete.

To manually clean up:

```bash
docker ps -a | grep testcontainers | awk '{print $1}' | xargs docker rm -f
```

## Integration with CI/CD

This test is designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run E2E Tests
  run: |
    cd backend/e2e-tests
    mvn verify
```

**Note**: Ensure CI environment has:

- Docker available
- Sufficient resources (4GB+ RAM)
- Network access for pulling images

## Related Documentation

- [Order Validation Implementation](../../ORDER_VALIDATION_IMPLEMENTATION.md)
- [Order Validation Testing Guide](../../ORDER_VALIDATION_TESTING.md)
- [E2E Test Setup](../E2E_SETUP_SUMMARY.md)

## Test Maintenance

### Updating Test Data

To modify product stock levels or quantities:

1. Edit test constants in `OrderValidationE2ETest.java`
2. Update `createSimpleProduct()` calls

### Adding More Scenarios

To add additional validation scenarios:

1. Create new test methods with `@Test` and `@Order(n)` annotations
2. Follow the existing pattern of async validation with Awaitility
3. Verify final order states

## Success Criteria

The test is successful when:

- ✅ All 12 test steps pass
- ✅ Order 1 is CANCELLED due to stock validation failure
- ✅ Order 2 is CONFIRMED after successful validation
- ✅ All async validations complete within timeout
- ✅ No infrastructure errors during execution
