# Complete Development Environment Setup

This document provides a comprehensive overview of the complete development environment configuration for the ecommerce microservices project.

## Services Configuration

### Infrastructure Services

1. **PostgreSQL Database** (Port 5432)

   - Primary database with separate schemas for each service
   - Auto-creates: `catalog_db`, `cart_db`, `order_db`, `security_db`
   - Credentials: `postgres/password`

2. **Redis Cache** (Port 6379)

   - Used by cart-service for session storage
   - Persistent storage enabled

3. **MinIO S3 Storage** (Ports 9000/9001)

   - S3-compatible storage for catalog images
   - Console: http://localhost:9001 (`minioadmin/minioadmin`)
   - Auto-creates `ecommerce-images` bucket

4. **Apache Kafka** (Ports 9092/9093)
   - Event streaming between services
   - Zookeeper dependency included
   - Kafka UI available at http://localhost:8085

### Spring Boot Services

1. **Eureka Server** (Port 8761)

   - Service discovery and registration
   - **Starts first** - all other services depend on it
   - Health check: http://localhost:8761/actuator/health

2. **Security Service** (Port 8081)

   - JWT authentication and authorization
   - **Starts second** - provides JWT validation for other services
   - Dependencies: Eureka Server
   - Health check: http://localhost:8081/actuator/health

3. **Gateway Service** (Port 8080)

   - API Gateway with routing and CORS configuration
   - Dependencies: Eureka Server, Security Service
   - Health check: http://localhost:8080/actuator/health

4. **Catalog Service** (Port 8082)

   - Product management with S3 image storage
   - Dependencies: Eureka, Security, Kafka, MinIO
   - Health check: http://localhost:8082/actuator/health

5. **Cart Service** (Port 8084)

   - Shopping cart with Redis session storage
   - Dependencies: Eureka, Security, Redis, Kafka
   - Health check: http://localhost:8084/actuator/health

6. **Order Service** (Port 8083)
   - Order processing and management
   - Dependencies: Eureka, Security, Kafka
   - Health check: http://localhost:8083/actuator/health

## Service Dependencies Matrix

| Service          | PostgreSQL | Redis | MinIO | Kafka | Eureka | Security |
| ---------------- | ---------- | ----- | ----- | ----- | ------ | -------- |
| Eureka Server    | ❌         | ❌    | ❌    | ❌    | ❌     | ❌       |
| Security Service | ✅         | ❌    | ❌    | ❌    | ✅     | ❌       |
| Gateway          | ❌         | ❌    | ❌    | ❌    | ✅     | ✅       |
| Catalog Service  | ✅         | ❌    | ✅    | ✅    | ✅     | ✅       |
| Cart Service     | ✅         | ✅    | ❌    | ✅    | ✅     | ✅       |
| Order Service    | ✅         | ❌    | ❌    | ✅    | ✅     | ✅       |

## Startup Order

The docker-compose file is configured with proper dependencies to ensure correct startup order:

1. **Infrastructure Services** (parallel startup)

   - PostgreSQL, Redis, MinIO, Zookeeper, Kafka

2. **Eureka Server** (waits for infrastructure)

3. **Security Service** (waits for Eureka)

4. **Gateway + Business Services** (wait for Security and their dependencies)
   - Gateway (waits for Eureka + Security)
   - Catalog Service (waits for Eureka + Security + Kafka + MinIO)
   - Cart Service (waits for Eureka + Security + Redis + Kafka)
   - Order Service (waits for Eureka + Security + Kafka)

## Configuration Profiles

All Spring services are configured with two profiles:

### Default Profile (Local Development)

- Services connect to localhost endpoints
- H2 in-memory databases for simplicity
- Direct Kafka connection on localhost:9092

### Docker Profile

- Services use container hostnames for communication
- Can optionally use PostgreSQL instead of H2
- Kafka connection via internal network (kafka:9093)

## Health Checks

All services include:

- Actuator endpoints enabled
- Health check endpoints at `/actuator/health`
- Startup grace period (60 seconds)
- Retry logic for failed health checks

## Usage

### Start Complete Environment

```bash
cd dev
docker-compose up -d
```

### Check All Services Status

```bash
docker-compose ps
```

### View Logs

```bash
docker-compose logs -f [service-name]
```

### Stop Environment

```bash
docker-compose down
```

### Clean Reset (removes all data)

```bash
docker-compose down -v
```

## Testing Service Communication

### Through Gateway (Port 8080)

- Authentication: http://localhost:8080/api/auth/
- Products: http://localhost:8080/api/catalog/
- Cart: http://localhost:8080/api/cart/
- Orders: http://localhost:8080/api/orders/

### Direct Service Access

- Eureka: http://localhost:8761
- Security: http://localhost:8081
- Catalog: http://localhost:8082
- Order: http://localhost:8083
- Cart: http://localhost:8084
- Kafka UI: http://localhost:8085
- MinIO Console: http://localhost:9001

## Troubleshooting

### Service Won't Start

1. Check if dependencies are healthy: `docker-compose ps`
2. View logs: `docker-compose logs [service-name]`
3. Verify port conflicts: `netstat -tulpn | grep [port]`

### Database Issues

- PostgreSQL logs: `docker logs ecommerce-postgres`
- Connect directly: `psql -h localhost -U postgres -d ecommerce`

### Kafka Issues

- Kafka UI: http://localhost:8085
- Check Kafka logs: `docker logs ecommerce-kafka`

### S3/MinIO Issues

- MinIO health: http://localhost:9000/minio/health/live
- Console access: http://localhost:9001

## Environment Variables

Key environment variables that can be overridden:

```bash
# Database
POSTGRES_DB=ecommerce
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password

# MinIO/S3
S3_ENDPOINT=http://minio:9000
AWS_ACCESS_KEY=minioadmin
AWS_SECRET_KEY=minioadmin
S3_BUCKET=ecommerce-images

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9093

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
```
