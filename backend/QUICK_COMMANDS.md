# Quick Commands for E2E Testing

## Build Docker Images - Choose Your Method

### Method 1: PowerShell Script (Recommended for Windows)

```powershell
.\build-images.ps1
```

### Method 2: Batch Script (Simple)

```cmd
build-images.bat
```

### Method 3: Maven Profile

```bash
mvn install -Pdocker-build -DskipTests
```

### Method 4: Individual Services (Manual)

```bash
# From backend/ directory
cd services/eureka-server && mvn jib:dockerBuild -DskipTests && cd ../..
cd services/gateway && mvn jib:dockerBuild -DskipTests && cd ../..
cd services/security-service && mvn jib:dockerBuild -DskipTests && cd ../..
cd services/catalog-service && mvn jib:dockerBuild -DskipTests && cd ../..
cd services/cart-service && mvn jib:dockerBuild -DskipTests && cd ../..
cd services/order-service && mvn jib:dockerBuild -DskipTests && cd ../..
```

## Complete Workflow

### Quick Start (PowerShell - Recommended)

```powershell
# Navigate to backend directory
cd backend

# Build all Java projects
mvn clean install -DskipTests

# Build all Docker images
.\build-images.ps1

# Run E2E tests
mvn verify -pl e2e-tests
```

### Quick Start (Maven Only)

```bash
# Navigate to backend directory
cd backend

# Build projects and Docker images in one command
mvn clean install -Pdocker-build -DskipTests

# Run E2E tests
mvn verify -pl e2e-tests
```

## Verify Docker Images

```bash
docker images | grep ecommerce
```

Expected output:

```
ecommerce/cart-service       latest    abc123    2 minutes ago   300MB
ecommerce/catalog-service    latest    def456    2 minutes ago   310MB
ecommerce/eureka-server      latest    ghi789    2 minutes ago   280MB
ecommerce/gateway            latest    jkl012    2 minutes ago   275MB
ecommerce/order-service      latest    mno345    2 minutes ago   305MB
ecommerce/security-service   latest    pqr678    2 minutes ago   295MB
```

## Rebuild Single Service

If you only changed one service:

```bash
cd backend/services/cart-service
mvn clean install -DskipTests
mvn jib:dockerBuild
```

## Clean Up Docker Images

```bash
# Remove all ecommerce images
docker rmi $(docker images 'ecommerce/*' -q)

# Remove specific image
docker rmi ecommerce/cart-service:latest
```

## Common Issues

### "Cannot find Docker"

- Start Docker Desktop
- Verify: `docker info`

### "Image build failed"

- Check service compiles: `mvn clean install`
- Check Docker has space: `docker system df`

### "Permission denied" (Linux/Mac)

```bash
chmod +x build-images.ps1
```

## Performance Tips

### Parallel Builds

The PowerShell script builds sequentially. For parallel builds:

```bash
# Maven parallel build (may cause issues with Jib)
mvn install -Pdocker-build -DskipTests -T 1C
```

### Incremental Builds

After first build, only rebuild changed services:

```bash
# Example: Only rebuild cart-service
cd services/cart-service
mvn jib:dockerBuild -DskipTests
```

Jib is smart about layers, so rebuilds are fast (~5 seconds).

## Script Comparison

| Method                 | Speed  | Ease of Use | Output                      |
| ---------------------- | ------ | ----------- | --------------------------- |
| `build-images.ps1`     | Medium | ⭐⭐⭐⭐⭐  | Colorful, progress tracking |
| `build-images.bat`     | Medium | ⭐⭐⭐⭐    | Simple output               |
| Maven `-Pdocker-build` | Fast   | ⭐⭐⭐      | Maven logging               |
| Manual                 | Slow   | ⭐          | Full control                |

## Recommended Workflow

**Daily Development:**

```powershell
.\build-images.ps1  # Fast, visual feedback
mvn verify -pl e2e-tests
```

**CI/CD Pipeline:**

```bash
mvn clean install -Pdocker-build -DskipTests  # Reproducible
mvn verify -pl e2e-tests
```

**Single Service Update:**

```bash
cd services/cart-service
mvn clean install -DskipTests && mvn jib:dockerBuild
cd ../..
mvn verify -pl e2e-tests -Dit.test=CartFlowE2ETest
```
