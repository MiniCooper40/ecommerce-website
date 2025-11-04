# E2E Testing Checklist

Use this checklist before running E2E tests to ensure everything is ready.

## Pre-Test Setup Checklist

### ☐ Docker Environment

- [ ] Docker Desktop/Daemon is installed
- [ ] Docker is running (`docker info` succeeds)
- [ ] Docker has sufficient resources allocated:
  - [ ] Memory: At least 8GB recommended
  - [ ] CPUs: At least 4 cores recommended
  - [ ] Disk: At least 20GB free space
- [ ] No conflicting containers running (`docker ps`)

### ☐ Development Tools

- [ ] Java JDK 17 or higher installed (`java -version`)
- [ ] Maven 3.6+ installed (`mvn -version`)
- [ ] Git installed (if cloning repository)

### ☐ Code Dependencies

- [ ] All shared libraries are built:
  - [ ] events-lib
  - [ ] security-lib
  - [ ] test-utils
- [ ] No compilation errors in any service
- [ ] All services can build successfully

### ☐ Docker Images

Run: `docker images | grep ecommerce`

Expected images (all should be present):

- [ ] ecommerce/eureka-server:latest
- [ ] ecommerce/security-service:latest
- [ ] ecommerce/catalog-service:latest
- [ ] ecommerce/cart-service:latest
- [ ] ecommerce/order-service:latest
- [ ] ecommerce/gateway:latest

If any are missing, build them:

```bash
cd backend
mvn clean install -DskipTests
mvn jib:dockerBuild -DskipTests
```

## Running Tests Checklist

### ☐ Pre-Execution

- [ ] Navigate to `backend/` directory
- [ ] No local services running on ports: 8080-8084, 8761, 5432, 9092, 6379
- [ ] Docker network is clean (no conflicting networks)

### ☐ Execution

```bash
# Recommended command
mvn verify -pl e2e-tests
```

### ☐ During Test Execution

Monitor for:

- [ ] Infrastructure containers start successfully
- [ ] Service containers start successfully
- [ ] Health checks pass for all services
- [ ] No connection refused errors in logs
- [ ] Tests execute sequentially without failures

## Post-Test Checklist

### ☐ Verify Test Results

- [ ] All tests passed (green checkmarks)
- [ ] No test failures or errors
- [ ] No skipped tests (unless intentional)

### ☐ Review Outputs

Check the following directories:

- [ ] `backend/e2e-tests/target/failsafe-reports/` - Test reports
- [ ] Console output shows "BUILD SUCCESS"

### ☐ Cleanup (Automatic)

Testcontainers automatically cleans up:

- [ ] All containers are stopped
- [ ] Docker network is removed
- [ ] No orphaned containers remain (`docker ps -a | grep ecommerce`)

## Troubleshooting Checklist

### If Tests Fail to Start

- [ ] Check Docker is running: `docker info`
- [ ] Check Docker has resources available
- [ ] Verify images exist: `docker images | grep ecommerce`
- [ ] Rebuild images if outdated: `mvn jib:dockerBuild -DskipTests`
- [ ] Check for port conflicts: `docker ps`
- [ ] Clear Docker cache if needed: `docker system prune`

### If Infrastructure Containers Fail

- [ ] PostgreSQL:
  - [ ] Port 5432 is not in use locally
  - [ ] Docker can pull `postgres:15-alpine`
- [ ] Kafka:
  - [ ] Port 9092/9093 not in use
  - [ ] Docker can pull `confluentinc/cp-kafka:7.5.0`
  - [ ] Sufficient memory allocated to Docker
- [ ] Redis:
  - [ ] Port 6379 not in use
  - [ ] Docker can pull `redis:7-alpine`

### If Service Containers Fail

Check logs and verify:

- [ ] Base image available: `docker pull eclipse-temurin:17-jre-alpine`
- [ ] Service image was built successfully
- [ ] Dependencies (Eureka, PostgreSQL, Kafka) started first
- [ ] No application errors in service logs

### If Tests Fail During Execution

- [ ] Review test logs in `target/failsafe-reports/`
- [ ] Check for API endpoint changes
- [ ] Verify test data builders create valid data
- [ ] Ensure authentication tokens are valid
- [ ] Check for timing issues (increase Awaitility timeout)

### If Cleanup Doesn't Happen

Manually clean up:

```bash
# Stop all ecommerce containers
docker ps -a | grep ecommerce | awk '{print $1}' | xargs docker rm -f

# Remove Docker network
docker network ls | grep ecommerce | awk '{print $1}' | xargs docker network rm
```

## CI/CD Checklist

### GitHub Actions / Jenkins

- [ ] Docker-in-Docker or Docker socket available
- [ ] Sufficient runner resources (8GB+ RAM recommended)
- [ ] Build step creates Docker images before test step
- [ ] Test reports are collected as artifacts
- [ ] Cleanup happens even if tests fail

### Example CI Pipeline Steps

1. [ ] Checkout code
2. [ ] Setup JDK 17
3. [ ] Cache Maven dependencies
4. [ ] Build project: `mvn clean install -DskipTests`
5. [ ] Build Docker images: `mvn jib:dockerBuild -DskipTests`
6. [ ] Run E2E tests: `mvn verify -pl e2e-tests`
7. [ ] Collect test reports from `target/failsafe-reports/`
8. [ ] Cleanup (automatic via Testcontainers)

## Development Workflow Checklist

### Before Committing Changes

If you modified a service:

- [ ] Build the service: `mvn clean install`
- [ ] Rebuild Docker image: `mvn jib:dockerBuild`
- [ ] Run E2E tests to verify no regressions
- [ ] Commit code and push

### Adding New E2E Tests

- [ ] Extend `E2ETestBase` class
- [ ] Use `AuthHelper` for authentication
- [ ] Use `TestDataBuilder` for test data
- [ ] Add `@Order` annotations for sequential execution
- [ ] Use Awaitility for eventual consistency checks
- [ ] Add descriptive `@DisplayName` annotations
- [ ] Run test locally before committing
- [ ] Update README with new test scenario

## Quick Reference

### Essential Commands

```bash
# Build everything and run E2E tests
mvn clean install && mvn jib:dockerBuild -DskipTests && mvn verify -pl e2e-tests

# Just rebuild images and run tests
mvn jib:dockerBuild -DskipTests && mvn verify -pl e2e-tests

# Run specific test
mvn verify -pl e2e-tests -Dit.test=CartFlowE2ETest

# Run with debug output
mvn verify -pl e2e-tests -X

# View test results
cat backend/e2e-tests/target/failsafe-reports/TEST-*.xml
```

### Service Ports (in tests)

| Service  | Docker Port | Test Access    |
| -------- | ----------- | -------------- |
| Gateway  | 8080        | `GATEWAY_URL`  |
| Security | 8081        | `SECURITY_URL` |
| Cart     | 8082        | `CART_URL`     |
| Catalog  | 8083        | `CATALOG_URL`  |
| Order    | 8084        | `ORDER_URL`    |
| Eureka   | 8761        | N/A (internal) |

### Common Issues & Quick Fixes

| Issue                  | Quick Fix                            |
| ---------------------- | ------------------------------------ |
| Image not found        | `mvn jib:dockerBuild -DskipTests`    |
| Port in use            | `docker-compose down`                |
| Timeout during startup | Increase Docker resources            |
| Kafka errors           | Wait 5-10 seconds longer for startup |
| Database errors        | Check PostgreSQL container logs      |

## Status Indicators

### ✅ Ready to Run Tests

All checkboxes in "Pre-Test Setup" are checked.

### ⚠️ Potential Issues

Some Docker images missing or outdated.

### ❌ Cannot Run Tests

Docker not running or missing critical dependencies.

---

**Last Updated**: January 2025
**E2E Tests Version**: 1.0.0
**Testcontainers Version**: 1.19.3
