# Development Environment

This directory contains the development environment setup for the ecommerce microservices project.

## Services Included

### MinIO (S3 Compatible Storage)

- **Purpose**: Mock S3 service for local development
- **S3 API Port**: 9000
- **Management Console**: http://localhost:9001
- **Credentials**:
  - Username: `minioadmin`
  - Password: `minioadmin`
- **Bucket**: `ecommerce-images` (created automatically)

### PostgreSQL Database

- **Purpose**: Alternative to H2 for more realistic development
- **Port**: 5432
- **Database**: `ecommerce`
- **Credentials**:
  - Username: `postgres`
  - Password: `password`
- **Additional Databases**: `catalog_db`, `cart_db`, `order_db`, `security_db`

### Redis

- **Purpose**: Caching layer (optional)
- **Port**: 6379

## Quick Start

1. **Start the development environment:**

   ```bash
   cd dev
   docker-compose up -d
   ```

2. **Check service health:**

   ```bash
   docker-compose ps
   ```

3. **Access MinIO Console:**

   - Open http://localhost:9001
   - Login with `minioadmin` / `minioadmin`
   - Verify that `ecommerce-images` bucket exists

4. **Stop the environment:**

   ```bash
   docker-compose down
   ```

5. **Clean up (removes all data):**
   ```bash
   docker-compose down -v
   ```

## Integration with Services

### Catalog Service S3 Configuration

The catalog service is already configured to work with this MinIO setup:

- **Local Development**: Uses `http://localhost:9000` as S3 endpoint
- **Docker Mode**: Uses `http://minio:9000` as S3 endpoint

### Environment Variables

The following environment variables can be used to override defaults:

```bash
S3_ENDPOINT=http://localhost:9000
AWS_REGION=us-east-1
AWS_ACCESS_KEY=minioadmin
AWS_SECRET_KEY=minioadmin
S3_BUCKET=ecommerce-images
```

## Testing S3 Functionality

1. **Generate Upload URL:**

   ```bash
   curl -X POST "http://localhost:8082/api/images/upload-url?fileName=test.jpg&contentType=image/jpeg"
   ```

2. **Upload a file using the presigned URL** (use the URL from step 1)

3. **Get public URL:**
   ```bash
   curl "http://localhost:8082/api/images/public-url/{key}"
   ```

## Troubleshooting

### MinIO Issues

- Check if the bucket was created: `docker logs ecommerce-minio-setup`
- Access MinIO directly: http://localhost:9000/minio/health/live

### Database Issues

- Check PostgreSQL logs: `docker logs ecommerce-postgres`
- Connect to database: `psql -h localhost -U postgres -d ecommerce`

### Network Issues

- All services are on the `ecommerce-network` network
- Services can communicate using container names as hostnames
