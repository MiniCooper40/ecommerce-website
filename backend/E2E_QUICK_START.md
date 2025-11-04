# E2E Testing Quick Start Guide

## Prerequisites

1. Docker installed and running
2. Maven 3.6+ installed
3. JDK 17 installed

## Step 1: Build Docker Images

From the `backend/` directory:

```bash
# Clean and build all modules
mvn clean install -DskipTests

# Build Docker images for all services using Jib
mvn jib:dockerBuild -DskipTests
```

This will create the following Docker images:

- `ecommerce/eureka-server:latest`
- `ecommerce/security-service:latest`
- `ecommerce/catalog-service:latest`
- `ecommerce/cart-service:latest`
- `ecommerce/order-service:latest`
- `ecommerce/gateway:latest`

## Step 2: Verify Images

```bash
docker images | grep ecommerce
```

You should see all 6 service images listed.

## Step 3: Run E2E Tests

```bash
# Run all E2E tests
mvn verify -pl e2e-tests

# Or navigate to e2e-tests directory
cd e2e-tests
mvn verify
```

## What Happens During Test Execution

1. **Infrastructure Startup** (~60-90 seconds):

   - PostgreSQL container starts
   - Kafka container starts
   - Redis container starts

2. **Service Startup** (~60-90 seconds):

   - Eureka Server starts and waits for health check
   - Security Service starts and registers with Eureka
   - Catalog Service starts and registers with Eureka
   - Cart Service starts and registers with Eureka
   - Order Service starts and registers with Eureka
   - Gateway starts and connects to Eureka

3. **Test Execution** (~5-10 seconds per test):

   - CartFlowE2ETest runs (7 test methods)
   - Tests validate complete cart flow
   - Tests verify eventual consistency via Kafka events

4. **Cleanup**:
   - All containers are stopped and removed
   - Docker network is cleaned up

## Expected Output

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.ecommerce.e2e.tests.CartFlowE2ETest
[INFO] Starting E2E test infrastructure...
[INFO] Starting PostgreSQL container...
[INFO] PostgreSQL started on port: 55432
[INFO] Starting Kafka container...
[INFO] Kafka started on: localhost:55433
[INFO] Starting Redis container...
[INFO] Redis started on port: 55434
[INFO] Starting Eureka Server...
[INFO] Eureka Server started on port: 55435
[INFO] Starting Security Service...
[INFO] Security Service started on port: 55436
[INFO] Starting Catalog Service...
[INFO] Catalog Service started on port: 55437
[INFO] Starting Cart Service...
[INFO] Cart Service started on port: 55438
[INFO] Starting Order Service...
[INFO] Order Service started on port: 55439
[INFO] Starting Gateway...
[INFO] Gateway started on port: 55440
[INFO] E2E test infrastructure started successfully
[INFO] Gateway URL: http://localhost:55440
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## Running Specific Tests

```bash
# Run only CartFlowE2ETest
mvn verify -pl e2e-tests -Dit.test=CartFlowE2ETest

# Run with debug logging
mvn verify -pl e2e-tests -X
```

## Troubleshooting

### Docker Not Running

**Error**: `Could not find a valid Docker environment`

**Solution**: Start Docker Desktop or Docker daemon

### Image Not Found

**Error**: `Image 'ecommerce/cart-service:latest' not found`

**Solution**: Build the Docker images first

```bash
mvn jib:dockerBuild -DskipTests
```

### Port Already in Use

**Error**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Solution**: Stop any locally running services

```bash
docker-compose down
# Or stop individual Docker containers
docker ps
docker stop <container-id>
```

### Tests Timeout

**Error**: `Container did not start within timeout`

**Solution**:

1. Check Docker resource allocation (increase memory/CPU in Docker Desktop)
2. Pull base image manually: `docker pull eclipse-temurin:17-jre-alpine`
3. Increase timeout in E2ETestBase.java

### Kafka Connection Errors

**Solution**: Tests may need a brief pause after Kafka starts. This is already handled in E2ETestBase with a 5-second delay.

## Next Steps

- Review `backend/e2e-tests/README.md` for detailed documentation
- Review `backend/e2e-tests/E2E_SETUP_SUMMARY.md` for setup details
- Add more E2E tests in `backend/e2e-tests/src/test/java/com/ecommerce/e2e/tests/`
- Extend test utilities in `backend/e2e-tests/src/test/java/com/ecommerce/e2e/util/`

## Additional Commands

### Rebuild Single Service Image

```bash
cd services/cart-service
mvn clean install -DskipTests
mvn jib:dockerBuild
```

### Clean Docker Images

```bash
# Remove all ecommerce images
docker rmi $(docker images 'ecommerce/*' -q)

# Or remove specific image
docker rmi ecommerce/cart-service:latest
```

### View Test Reports

After running tests, view detailed reports:

```bash
# Navigate to
backend/e2e-tests/target/failsafe-reports/
```

## Documentation

- **Full Documentation**: `backend/e2e-tests/README.md`
- **Setup Summary**: `backend/e2e-tests/E2E_SETUP_SUMMARY.md`
- **Example Test**: `backend/e2e-tests/src/test/java/com/ecommerce/e2e/tests/CartFlowE2ETest.java`

## Support

For issues:

1. Check `backend/e2e-tests/README.md` troubleshooting section
2. Review logs in `backend/e2e-tests/target/failsafe-reports/`
3. Ensure all prerequisites are met
4. Verify Docker has sufficient resources
