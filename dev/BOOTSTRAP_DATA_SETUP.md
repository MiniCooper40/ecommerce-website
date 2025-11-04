# Bootstrap Data Setup Guide

This guide explains how the sample products and admin user are automatically created when you start the ecommerce platform.

## What Gets Bootstrapped

### 1. Admin User (Security Service)

- **Email**: `admin@ecommerce.com`
- **Password**: `admin123`
- **Roles**: ADMIN, USER

### 2. Sample Products (Catalog Service)

- **12 products** across 5 categories
- **33 product images** stored in MinIO/S3
- All products active with stock

## Setup Instructions

### Step 1: Generate Placeholder Images (One-time)

```powershell
cd dev\minio-seed-data
.\generate-placeholders.ps1
```

**Options:**

- **With ImageMagick**: Generates colored placeholder JPEGs automatically
- **Without ImageMagick**: Creates a list of needed images for manual creation
- **Skip it**: Use your own product images (just name them correctly)

### Step 2: Start Development Environment

```powershell
cd dev
docker-compose -f docker-compose-full.yml up -d
```

The `minio-setup` container will:

1. Create the `ecommerce-images` bucket
2. Set it to public access
3. Upload all images from `minio-seed-data/products/` to `products/` in the bucket

### Step 3: Start Backend Services

```powershell
# Terminal 1 - Security Service
cd backend\services\security-service
mvn spring-boot:run

# Terminal 2 - Catalog Service
cd backend\services\catalog-service
mvn spring-boot:run
```

**On first run**, each service will:

- Security: Create roles and admin user
- Catalog: Create 12 products with image records pointing to MinIO

### Step 4: Verify Bootstrap Data

**Check Admin User:**

```powershell
# Login as admin
curl -X POST http://localhost:8081/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"admin@ecommerce.com","password":"admin123"}'
```

**Check Products:**

```powershell
# Get all products
curl http://localhost:8082/api/catalog/products
```

**Check MinIO:**

- Open: http://localhost:9001
- Login: `minioadmin` / `minioadmin`
- Navigate to `ecommerce-images` bucket
- Verify `products/` folder contains 33 images

## Image Path Mapping

The bootstrap code uses paths like:

```
/images/products/headphones-1.jpg
```

These are converted to S3 keys:

```
products/headphones-1.jpg
```

Which are stored in MinIO at:

```
http://localhost:9000/ecommerce-images/products/headphones-1.jpg
```

## Customizing Bootstrap Data

### Add Your Own Product Images

1. Place images in: `dev\minio-seed-data\products\`
2. Use exact filenames from the bootstrap code (see README)
3. Restart minio-setup: `docker-compose restart minio-setup`

### Modify Products

Edit: `backend\services\catalog-service\src\main\java\com\ecommerce\catalog\service\DataInitializationService.java`

### Change Admin Credentials

Edit: `backend\services\security-service\src\main\java\com\ecommerce\security\service\DataInitializationService.java`

## Re-running Bootstrap

The bootstrap only runs when databases are **empty**. To re-run:

**Option 1: Clean Database**

```powershell
docker-compose down -v  # Deletes volumes
docker-compose up -d    # Fresh start
```

**Option 2: Delete Specific Data**

```sql
-- For products
DELETE FROM product_images;
DELETE FROM products;

-- For admin user
DELETE FROM users WHERE email = 'admin@ecommerce.com';
```

## Troubleshooting

### "No seed data found - skipping image upload"

**Cause**: No images in `dev\minio-seed-data\products\`

**Fix**: Run `generate-placeholders.ps1` or add images manually

### "Image with S3 key already exists"

**Cause**: Image record already in database but different product

**Fix**: Clean database or use unique S3 keys

### "Failed to create image for product"

**Cause**: S3Service can't generate presigned URL (MinIO not running)

**Fix**: Ensure MinIO is healthy: `docker-compose ps minio`

### Images not appearing in products

**Check:**

1. MinIO has the images: http://localhost:9001
2. Bucket is public: `mc anonymous get myminio/ecommerce-images`
3. Image records created: Check `product_images` table
4. S3 keys are correct: Should be `products/filename.jpg` not `/images/products/filename.jpg`

## Production Considerations

### Security

- **Change admin password** immediately
- Use strong passwords stored in secrets management
- Disable bootstrap in production (check for empty DB first)

### Images

- Replace placeholders with real product images
- Use CDN for image delivery
- Implement image optimization pipeline
- Consider using AWS S3 instead of MinIO

### Data

- Don't bootstrap sample products in production
- Use proper data migration scripts
- Have separate admin user creation process
- Implement proper onboarding workflows
