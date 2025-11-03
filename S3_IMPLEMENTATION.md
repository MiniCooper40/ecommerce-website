# S3 Presigned URL Implementation for Catalog Service

## Overview

This implementation adds S3 presigned URL functionality to the catalog service, enabling direct client-to-S3 file uploads for product images. The solution uses MinIO as a mock S3 service for development.

## Implementation Components

### 1. Dependencies Added

```xml
<!-- AWS SDK S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.21.29</version>
</dependency>

<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>url-connection-client</artifactId>
    <version>2.21.29</version>
</dependency>
```

### 2. Configuration Classes

#### S3Config.java

- Configures S3Client and S3Presigner beans
- Supports both AWS S3 (production) and MinIO (development)
- Uses path-style access for MinIO compatibility
- Environment-based configuration

#### Application Properties

```yaml
aws:
  s3:
    endpoint: ${S3_ENDPOINT:http://localhost:9000} # MinIO for dev
    region: ${AWS_REGION:us-east-1}
    access-key: ${AWS_ACCESS_KEY:minioadmin}
    secret-key: ${AWS_SECRET_KEY:minioadmin}
    bucket: ${S3_BUCKET:ecommerce-images}
```

### 3. Service Layer

#### S3Service.java

- **generatePresignedUploadUrl()**: Creates presigned URLs for file uploads (15 min expiry)
- **generatePresignedDownloadUrl()**: Creates presigned URLs for file access (1 hour expiry)
- **getPublicUrl()**: Returns public URLs for objects (when bucket is public)
- **generateObjectKey()**: Creates unique S3 object keys with UUID and timestamp

#### Key Features:

- Unique file naming with UUID to prevent conflicts
- Proper content type handling
- Configurable expiration times
- Support for both MinIO and AWS S3 URLs

### 4. REST API

#### ImageController.java

**Endpoints:**

1. **POST /api/catalog/images/upload-url**

   - Parameters: `fileName`, `contentType`, `fileSize` (optional)
   - Returns: Presigned upload URL, object key, bucket name, expiration
   - Validates: Content type (images only), file size (10MB max)

2. **POST /api/catalog/images/bulk-upload-urls**

   - Body: JSON with array of file requests
   - Returns: Array of presigned upload URLs
   - Validates: Max 20 files per request, content types, file sizes
   - Optimized for multiple file uploads

3. **GET /api/catalog/images/download-url/{key}**

   - Returns: Presigned download URL for viewing/downloading

4. **GET /api/catalog/images/public-url/{key}**
   - Returns: Public URL for accessing images (when bucket is public)

## Development Environment

### MinIO Setup

The `dev/` directory contains:

- `docker-compose.yml`: Full dev environment (MinIO, PostgreSQL, Redis)
- `docker-compose-minio-only.yml`: MinIO-only setup
- `dev-env.ps1` / `dev-env.sh`: Management scripts
- `README.md`: Detailed setup instructions

### Quick Start

1. **Start MinIO:**

   ```powershell
   cd dev
   .\dev-env.ps1 start
   ```

2. **Access MinIO Console:**

   - URL: http://localhost:9001
   - Credentials: minioadmin / minioadmin

3. **Start Catalog Service:**
   ```powershell
   cd backend/services/catalog-service
   mvn spring-boot:run
   ```

## Usage Flow

### 1. Client Requests Upload URL (Single File)

```bash
curl -X POST "http://localhost:8082/api/catalog/images/upload-url?fileName=product.jpg&contentType=image/jpeg"
```

**Response:**

```json
{
  "uploadUrl": "http://localhost:9000/ecommerce-images/catalog/images/uuid/123456789.jpg?...",
  "key": "products/uuid/123456789.jpg",
  "bucket": "ecommerce-images",
  "expiresInMinutes": 15
}
```

### 1b. Client Requests Upload URLs (Bulk)

```bash
curl -X POST "http://localhost:8082/api/catalog/images/bulk-upload-urls" \
  -H "Content-Type: application/json" \
  -d '{
    "files": [
      {
        "fileName": "product1.jpg",
        "contentType": "image/jpeg",
        "fileSize": 2048000
      },
      {
        "fileName": "product2.png",
        "contentType": "image/png",
        "fileSize": 1536000
      }
    ]
  }'
```

**Response:**

```json
{
  "uploadUrls": [
    {
      "uploadUrl": "http://localhost:9000/ecommerce-images/catalog/images/uuid1/product1.jpg?...",
      "key": "products/uuid1/product1.jpg",
      "bucket": "ecommerce-images",
      "expiresInMinutes": 15
    },
    {
      "uploadUrl": "http://localhost:9000/ecommerce-images/catalog/images/uuid2/product2.png?...",
      "key": "products/uuid2/product2.png",
      "bucket": "ecommerce-images",
      "expiresInMinutes": 15
    }
  ]
}
```

### 2. Client Uploads Directly to S3

```bash
curl -X PUT "PRESIGNED_URL" \
  -H "Content-Type: image/jpeg" \
  --data-binary @product.jpg
```

### 3. Client Saves Product with Image Key

The client can now save the product information including the S3 object key for future reference.

### 4. Viewing Images

```bash
# Get public URL (if bucket is public)
curl "http://localhost:8082/api/catalog/images/public-url/catalog/images/uuid/123456789.jpg"

# Get presigned download URL (for private buckets)
curl "http://localhost:8082/api/catalog/images/download-url/catalog/images/uuid/123456789.jpg"
```

## Security Considerations

1. **File Type Validation**: Only image types allowed (jpeg, png, gif, webp)
2. **File Size Limits**: Maximum 10MB per file
3. **Unique Naming**: UUID-based naming prevents conflicts and guessing
4. **Presigned URL Expiry**: Short-lived URLs (15 minutes for upload, 1 hour for download)
5. **Content Type Enforcement**: S3 enforces the specified content type

## Environment Configuration

### Local Development

- MinIO endpoint: `http://localhost:9000`
- Bucket: `ecommerce-images` (public)
- Credentials: `minioadmin` / `minioadmin`

### Docker Development

- MinIO endpoint: `http://minio:9000`
- Same bucket and credentials

### Production (AWS S3)

- Remove `endpoint` configuration
- Use real AWS credentials
- Consider private bucket with CloudFront distribution

## Testing

1. **Start Development Environment:**

   ```powershell
   .\dev\dev-env.ps1 start
   ```

2. **Build and Start Catalog Service:**

   ```powershell
   cd backend\services\catalog-service
   mvn spring-boot:run
   ```

3. **Test S3 Functionality:**

   ```powershell
   .\dev\dev-env.ps1 test-s3
   ```

4. **Verify in MinIO Console:**
   - Open http://localhost:9001
   - Check `ecommerce-images` bucket
   - Upload a test file using the presigned URL

## Troubleshooting

1. **"Bucket does not exist"**: Check MinIO setup container logs
2. **Access denied**: Verify MinIO credentials match configuration
3. **Network issues**: Ensure MinIO is accessible on port 9000
4. **Invalid presigned URL**: Check system clock synchronization

## Future Enhancements

1. **Image Processing**: Resize/optimize images on upload
2. **CDN Integration**: Add CloudFront for production
3. **Metadata Storage**: Store image metadata in database
4. **Batch Operations**: Support multiple file uploads ✅ (Implemented)
5. **Image Variants**: Generate thumbnails automatically

## Bulk Upload Benefits

The bulk upload endpoint (`/api/catalog/images/bulk-upload-urls`) provides several advantages over multiple individual requests:

1. **Reduced HTTP Overhead**: One request instead of 5-10 individual requests
2. **Better Performance**: Lower latency and network overhead
3. **Atomic Operations**: All URLs generated together or none at all
4. **Rate Limiting Friendly**: Fewer API calls against rate limits
5. **Better UX**: Frontend can show progress for the entire batch

**Frontend Usage Pattern:**

```javascript
// Instead of multiple requests:
// for (const file of files) {
//   await fetch('/api/catalog/images/upload-url?...')
// }

// Single bulk request:
const response = await fetch("/api/catalog/images/bulk-upload-urls", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    files: files.map((file) => ({
      fileName: file.name,
      contentType: file.type,
      fileSize: file.size,
    })),
  }),
});

const { uploadUrls } = await response.json();

// Upload files in parallel using presigned URLs
await Promise.all(
  uploadUrls.map(async (urlData, index) => {
    await fetch(urlData.uploadUrl, {
      method: "PUT",
      body: files[index],
      headers: { "Content-Type": files[index].type },
    });
  })
);
```
