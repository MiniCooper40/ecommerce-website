# E2E Testing Infrastructure

This module provides comprehensive End-to-End testing infrastructure for the ecommerce microservices platform using Testcontainers and Maven.

## Overview

The E2E tests spin up the entire microservices architecture using Docker containers via Testcontainers, allowing for realistic integration testing in an isolated environment.

## Architecture

The test infrastructure includes:

- **Infrastructure Containers**:

  - PostgreSQL (shared database)
  - Apache Kafka (event streaming)
  - Redis (cart session storage)

- **Service Containers**:
  - Eureka Server (service discovery)
  - Security Service (authentication/authorization)
  - Catalog Service (product management)
  - Cart Service (shopping cart)
  - Order Service (order processing)
  - Gateway (API gateway)

## Prerequisites

1. **Docker**: Must be installed and running
2. **Maven**: Version 3.6+ required
3. **Java**: JDK 17 or higher
4. **Docker Images**: All service images must be built first

## Building Docker Images

Before running E2E tests, build Docker images for all services:

### Option 1: Build All Services at Once

From the `backend/` directory:

```bash
# Clean and install all modules
mvn clean install -DskipTests

# Build all Docker images using Jib
mvn jib:dockerBuild -DskipTests
```

### Option 2: Build Individual Services

For each service (from `backend/services/<service-name>/`):

```bash
mvn clean install -DskipTests
mvn jib:dockerBuild
```

Services to build:

- `eureka-server`
- `security-service`
- `catalog-service`
- `cart-service`
- `order-service`
- `gateway`

### Verify Images

Check that all images were created:

```bash
docker images | grep ecommerce
```

Expected output:

```
ecommerce/eureka-server      latest
ecommerce/security-service   latest
ecommerce/catalog-service    latest
ecommerce/cart-service       latest
ecommerce/order-service      latest
ecommerce/gateway            latest
```

## Running E2E Tests

### Run All E2E Tests

From the `backend/` directory:

```bash
mvn verify -pl e2e-tests
```

Or from the `backend/e2e-tests/` directory:

```bash
mvn verify
```

### Run Specific Test Class

```bash
mvn verify -pl e2e-tests -Dit.test=CartFlowE2ETest
```

### Run with Verbose Logging

```bash
mvn verify -pl e2e-tests -X
```

## Test Structure

### Base Test Class

`E2ETestBase.java` provides shared infrastructure:

```java
@BeforeAll
public static void setUpInfrastructure() {
    // Starts all containers
}

@AfterAll
public static void tearDownInfrastructure() {
    // Stops all containers
}
```

All E2E tests should extend this class:

```java
class MyE2ETest extends E2ETestBase {
    // Test methods
}
```

### Utilities

**AuthHelper**: Authentication token management

```java
String token = AuthHelper.getUserToken(SECURITY_URL, "username");
```

**TestDataBuilder**: Test data creation

```java
Map<String, Object> product = TestDataBuilder.createSimpleProduct("Product", 99.99);
Map<String, Object> addToCart = TestDataBuilder.createAddToCartRequest(productId, 2);
```

## Writing E2E Tests

### Example Test

```java
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MyFeatureE2ETest extends E2ETestBase {

    private static String userToken;
    private static Long resourceId;

    @BeforeAll
    static void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        userToken = AuthHelper.getUserToken(SECURITY_URL, "testuser");
    }

    @Test
    @Order(1)
    @DisplayName("Create resource via API")
    void testCreateResource() {
        Map<String, Object> request = Map.of("name", "Test Resource");

        Response response = given()
                .baseUri(CATALOG_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(request)
            .when()
                .post("/catalog/products")
            .then()
                .statusCode(201)
                .body("name", equalTo("Test Resource"))
                .extract()
                .response();

        resourceId = response.jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    @DisplayName("Verify eventual consistency")
    void testEventualConsistency() {
        // Use Awaitility for async operations
        await()
                .atMost(10, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    given()
                            .baseUri(CART_URL)
                            .header("Authorization", "Bearer " + userToken)
                        .when()
                            .get("/cart")
                        .then()
                            .statusCode(200)
                            .body("items", hasSize(greaterThan(0)));
                });
    }
}
```

### Best Practices

1. **Use @Order**: Tests should be ordered to create a realistic flow
2. **Test Eventually Consistent Operations**: Use Awaitility for Kafka-based updates
3. **Reuse Tokens**: Cache authentication tokens using `AuthHelper`
4. **Clear Between Tests**: Use `@AfterAll` to clean up if needed
5. **Log Progress**: Use SLF4J logging for test visibility

## Service URLs

Available service URLs in tests:

- `GATEWAY_URL`: API Gateway endpoint
- `CATALOG_URL`: Catalog Service direct endpoint
- `CART_URL`: Cart Service direct endpoint
- `ORDER_URL`: Order Service direct endpoint
- `SECURITY_URL`: Security Service direct endpoint

## Configuration

### Docker Profile

All services use the `docker` Spring profile when running in containers. This profile:

- Uses service names instead of `localhost` (e.g., `postgres` instead of `localhost:5432`)
- Configures inter-service communication via Docker network
- Sets appropriate timeouts and health checks

### Test Timeouts

Container startup timeouts are configured in `E2ETestBase`:

```java
.waitingFor(Wait.forHttp("/actuator/health")
        .forPort(8081)
        .withStartupTimeout(Duration.ofMinutes(3)))
```

Increase if services take longer to start on your machine.

## Troubleshooting

### Tests Fail with "Container startup failed"

**Solution**: Ensure Docker images are built and Docker daemon is running

```bash
docker info
mvn jib:dockerBuild -DskipTests
```

### "Image not found" Error

**Solution**: Build the missing service image

```bash
cd backend/services/<service-name>
mvn jib:dockerBuild -DskipTests
```

### Kafka Connection Errors

**Solution**: Increase Kafka startup timeout or add delay

```java
Thread.sleep(5000); // After Kafka container starts
```

### Port Already in Use

**Solution**: Stop any locally running services

```bash
docker-compose down
# Or stop individual services
```

### Slow Test Execution

**Causes**:

- Cold Docker image pulls
- Insufficient Docker resources

**Solutions**:

- Pre-pull base images: `docker pull eclipse-temurin:17-jre-alpine`
- Increase Docker resources in Docker Desktop settings
- Use Docker image caching

### Database Initialization Errors

**Solution**: Check PostgreSQL container logs

```java
postgresContainer.getLogs(); // In test
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  e2e-tests:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Build Docker Images
        run: |
          cd backend
          mvn clean install -DskipTests
          mvn jib:dockerBuild -DskipTests

      - name: Run E2E Tests
        run: |
          cd backend
          mvn verify -pl e2e-tests
```

## Test Coverage

Current E2E test scenarios:

- ✅ **CartFlowE2ETest**: Complete cart flow (add, update, view, remove)

### Planned Tests

- Order placement and processing
- Product search and filtering
- User registration and authentication
- Multi-service transaction flows
- Event-driven consistency verification

## Performance

Typical test execution times:

- Infrastructure startup: ~60-90 seconds
- Individual test: ~5-10 seconds
- Full suite: ~2-3 minutes

## Additional Resources

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [REST Assured Documentation](https://rest-assured.io/)
- [Awaitility Documentation](https://github.com/awaitility/awaitility)
- [Jib Maven Plugin](https://github.com/GoogleContainerTools/jib)

## Support

For issues or questions:

1. Check logs in `target/failsafe-reports/`
2. Review container logs via Testcontainers
3. Verify Docker resource allocation
4. Ensure all prerequisites are met
