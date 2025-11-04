# E2E Testing Infrastructure - Setup Summary

## Overview

Comprehensive End-to-End testing infrastructure has been successfully configured for the ecommerce microservices platform. The setup enables full integration testing using Testcontainers, Maven, and Docker.

## What Was Created

### 1. E2E Tests Maven Module (`backend/e2e-tests/`)

**Files Created**:

- `pom.xml` - Maven configuration with Testcontainers, REST Assured, Awaitility dependencies
- `README.md` - Comprehensive documentation for using the E2E testing infrastructure

**Dependencies Configured**:

- Testcontainers 1.19.3 (PostgreSQL, Kafka, JUnit Jupiter)
- REST Assured 5.3.2 (API testing)
- Awaitility 4.2.0 (async operation testing)
- Events Library (shared event definitions)

**Maven Configuration**:

- Failsafe plugin for integration tests
- Tests named `*E2ETest.java` or `*IT.java` are executed during `mvn verify`
- Surefire plugin disabled (no unit tests in this module)

### 2. Base Test Infrastructure

**`E2ETestBase.java`** (`src/test/java/com/ecommerce/e2e/config/`)

Provides shared Testcontainers infrastructure:

- **Infrastructure Containers**:

  - PostgreSQL 15 (shared database for all services)
  - Kafka (Confluent Platform 7.5.0)
  - Redis 7 (cart session storage)

- **Service Containers**:

  - Eureka Server (port 8761)
  - Security Service (port 8081)
  - Catalog Service (port 8083)
  - Cart Service (port 8082)
  - Order Service (port 8084)
  - Gateway (port 8080)

- **Features**:
  - Shared Docker network for inter-service communication
  - Health check waiting strategies with 2-3 minute timeouts
  - Service URLs exposed as static variables (`GATEWAY_URL`, `CART_URL`, etc.)
  - Automatic startup/shutdown lifecycle management
  - Dynamic property registration for Spring tests

### 3. Test Utilities

**`AuthHelper.java`** (`src/test/java/com/ecommerce/e2e/util/`)

Authentication helper with:

- `registerAndGetToken()` - Register new user and get JWT
- `loginAndGetToken()` - Login existing user and get JWT
- `getAdminToken()` - Get admin user token
- `getUserToken()` - Get regular user token
- Token caching to avoid redundant authentication calls

**`TestDataBuilder.java`** (`src/test/java/com/ecommerce/e2e/util/`)

Test data builders for:

- Product creation requests with images, pricing, inventory
- Add-to-cart requests
- Update cart item requests
- Checkout requests
- Random data generation (usernames, emails, product names)

### 4. Example E2E Test

**`CartFlowE2ETest.java`** (`src/test/java/com/ecommerce/e2e/tests/`)

Demonstrates complete shopping cart flow:

1. **Admin creates product** in catalog service
2. **User adds product to cart** via cart service
3. **User views cart** with denormalized product data
4. **User updates cart item quantity**
5. **User verifies updated cart** (eventual consistency via Kafka events)
6. **User removes product from cart**
7. **User verifies empty cart**

**Key Features**:

- Uses `@Order` annotation for sequential test execution
- Implements Awaitility for testing eventual consistency (Kafka events)
- Demonstrates REST Assured usage for API testing
- Shows proper authentication token usage
- Validates cross-service communication

### 5. Logging Configuration

**`logback-test.xml`** (`src/test/resources/`)

Configured logging levels:

- E2E tests: INFO
- Testcontainers: INFO
- Docker Java client: WARN
- REST Assured: WARN
- Kafka/Spring: WARN

## Docker Image Build Configuration

Added **Jib Maven Plugin** to all service `pom.xml` files:

### Services Updated:

- ✅ cart-service (port 8082)
- ✅ catalog-service (port 8083)
- ✅ eureka-server (port 8761)
- ✅ gateway (port 8080)
- ✅ security-service (port 8081)
- ✅ order-service (port 8084)

### Jib Configuration:

```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.4.0</version>
    <configuration>
        <from>
            <image>eclipse-temurin:17-jre-alpine</image>
        </from>
        <to>
            <image>ecommerce/<service-name></image>
            <tags>
                <tag>latest</tag>
                <tag>${project.version}</tag>
            </tags>
        </to>
        <container>
            <ports><port>SERVICE_PORT</port></ports>
            <environment>
                <SPRING_PROFILES_ACTIVE>docker</SPRING_PROFILES_ACTIVE>
            </environment>
        </container>
    </configuration>
</plugin>
```

### Build Command:

```bash
mvn jib:dockerBuild
```

## Spring Profile Configuration

### Docker Profile Status

All services already have **`docker` profile** configured in `application.yml`:

- ✅ **cart-service**: Docker profile with postgres, kafka, redis service names
- ✅ **catalog-service**: Docker profile with postgres, kafka, S3/MinIO service names
- ✅ **security-service**: Docker profile with postgres service name
- ✅ **order-service**: Docker profile with postgres, kafka service names
- ✅ **gateway**: Docker profile with eureka-server service name
- ✅ **eureka-server**: Docker profile with eureka-server hostname

### Profile Differences

**Development (default)**:

- Uses `localhost` for all services
- PostgreSQL on `localhost:5434`
- Kafka on `localhost:9092`
- Redis on `localhost:6379`
- Eureka on `localhost:8761`

**Docker Profile**:

- Uses Docker service names (e.g., `postgres`, `kafka`, `redis`)
- PostgreSQL on `postgres:5432`
- Kafka on `kafka:9093`
- Redis on `redis:6379`
- Eureka on `eureka-server:8761`
- JWT endpoints use service names (e.g., `http://security-service:8081`)

## Parent POM Updates

**`backend/pom.xml`** - Added e2e-tests module:

```xml
<modules>
    <!-- existing modules -->
    <module>e2e-tests</module>
</modules>
```

## How to Use

### 1. Build Docker Images

From `backend/` directory:

```bash
# Build all modules
mvn clean install -DskipTests

# Build all Docker images
mvn jib:dockerBuild -DskipTests
```

### 2. Run E2E Tests

```bash
# Run all E2E tests
mvn verify -pl e2e-tests

# Run specific test
mvn verify -pl e2e-tests -Dit.test=CartFlowE2ETest

# Run with verbose output
mvn verify -pl e2e-tests -X
```

### 3. Verify Setup

Check Docker images:

```bash
docker images | grep ecommerce
```

Expected output:

```
ecommerce/cart-service       latest
ecommerce/catalog-service    latest
ecommerce/eureka-server      latest
ecommerce/gateway            latest
ecommerce/order-service      latest
ecommerce/security-service   latest
```

## Architecture Flow

```
Test Execution
    ↓
E2ETestBase.setUpInfrastructure()
    ↓
[Start Infrastructure]
    - PostgreSQL Container
    - Kafka Container
    - Redis Container
    ↓
[Start Services]
    - Eureka Server
    - Security Service
    - Catalog Service
    - Cart Service
    - Order Service
    - Gateway
    ↓
[Tests Execute]
    - Use AuthHelper for tokens
    - Use TestDataBuilder for data
    - Call APIs via REST Assured
    - Assert responses
    - Wait for eventual consistency (Awaitility)
    ↓
E2ETestBase.tearDownInfrastructure()
    ↓
[Stop All Containers]
```

## Key Benefits

1. **Isolated Testing**: Each test run uses fresh containers
2. **Realistic Environment**: Full microservices stack with real dependencies
3. **Docker-Based**: Same environment as production
4. **Maven Integrated**: Standard Maven lifecycle (`mvn verify`)
5. **No Manual Setup**: Testcontainers handles all infrastructure
6. **Fast Iteration**: Jib enables quick Docker image rebuilds
7. **CI/CD Ready**: Can run in GitHub Actions, Jenkins, etc.

## Testing Patterns Demonstrated

1. **Sequential Test Flow**: Using `@Order` for realistic user journeys
2. **Eventual Consistency**: Using Awaitility to wait for Kafka event processing
3. **Cross-Service Testing**: Product created in catalog, consumed by cart
4. **Authentication**: JWT tokens from security service used across services
5. **API Testing**: REST Assured for HTTP request/response validation
6. **Data Builders**: Reusable test data creation patterns

## Next Steps

### Suggested Additional Tests

1. **Order Flow E2E Test**:

   - Create product → Add to cart → Checkout → Place order → Verify order

2. **Product Search E2E Test**:

   - Create multiple products → Search by criteria → Verify results

3. **User Registration Flow**:

   - Register → Login → Update profile → Verify JWT claims

4. **Event-Driven Consistency Test**:

   - Update product in catalog → Verify cart reflects changes
   - Delete product → Verify cart removes invalid items

5. **Multi-User Scenario**:
   - Multiple users adding same product
   - Inventory validation
   - Cart isolation between users

### Performance Testing

Can be extended with:

- JMeter integration
- Gatling for load testing
- Performance assertions (response time thresholds)

### Test Data Management

Can add:

- Database seeding utilities
- SQL scripts for common scenarios
- Test data cleanup strategies

## Files Modified/Created

### Created:

- `backend/e2e-tests/pom.xml`
- `backend/e2e-tests/README.md`
- `backend/e2e-tests/src/test/java/com/ecommerce/e2e/config/E2ETestBase.java`
- `backend/e2e-tests/src/test/java/com/ecommerce/e2e/util/AuthHelper.java`
- `backend/e2e-tests/src/test/java/com/ecommerce/e2e/util/TestDataBuilder.java`
- `backend/e2e-tests/src/test/java/com/ecommerce/e2e/tests/CartFlowE2ETest.java`
- `backend/e2e-tests/src/test/resources/logback-test.xml`

### Modified:

- `backend/pom.xml` (added e2e-tests module)
- `backend/services/cart-service/pom.xml` (added Jib plugin)
- `backend/services/catalog-service/pom.xml` (added Jib plugin)
- `backend/services/eureka-server/pom.xml` (added Jib plugin)
- `backend/services/gateway/pom.xml` (added Jib plugin)
- `backend/services/security-service/pom.xml` (added Jib plugin)
- `backend/services/order-service/pom.xml` (added Jib plugin)

## Summary

✅ Complete E2E testing infrastructure created
✅ Docker image building configured for all services
✅ Example test demonstrating full cart flow
✅ Utilities for authentication and test data creation
✅ Comprehensive documentation provided
✅ Spring profiles validated for dev/docker environments
✅ Maven lifecycle integration complete

The E2E testing infrastructure is now ready to use. Run `mvn jib:dockerBuild -DskipTests` to build images, then `mvn verify -pl e2e-tests` to run the tests.
