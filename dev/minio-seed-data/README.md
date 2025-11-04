# MinIO Seed Data

This directory contains placeholder images that are automatically uploaded to MinIO when the development environment starts.

## Directory Structure

```
minio-seed-data/
└── products/
    ├── headphones-1.jpg
    ├── headphones-2.jpg
    ├── fitness-watch-1.jpg
    ├── ... (all bootstrap product images)
```

## Image Requirements

- **Format**: JPEG, PNG, or WebP
- **Size**: Keep under 1MB for faster uploads
- **Naming**: Must match the paths referenced in `DataInitializationService.java`

## How It Works

1. When `docker-compose up` runs, the `minio-setup` container:

   - Creates the `ecommerce-images` bucket
   - Sets it to public access
   - Uploads all files from `./minio-seed-data/products/` to `ecommerce-images/products/`

2. The catalog service bootstrap uses these S3 keys:
   - `products/headphones-1.jpg`
   - `products/headphones-2.jpg`
   - etc.

## Adding Your Own Images

1. Place image files in the `products/` directory
2. Use the exact names referenced in the bootstrap code
3. Restart the minio-setup container: `docker-compose restart minio-setup`

## Placeholder Images

If you don't have actual product images yet, you can:

1. **Use the provided script** to generate solid color placeholders:

   ```bash
   ./generate-placeholders.ps1
   ```

2. **Download free product images** from:

   - https://unsplash.com
   - https://pexels.com
   - https://placeholder.com

3. **Create simple placeholders** with any image editor and save with the correct names

## Image List (Required for Bootstrap)

The following images are referenced by the bootstrap data:

### Electronics

- headphones-1.jpg
- headphones-2.jpg
- fitness-watch-1.jpg
- fitness-watch-2.jpg
- fitness-watch-3.jpg
- webcam-1.jpg
- webcam-2.jpg

### Home & Kitchen

- coffee-maker-1.jpg
- coffee-maker-2.jpg
- cookware-1.jpg
- cookware-2.jpg
- cookware-3.jpg
- robot-vacuum-1.jpg
- robot-vacuum-2.jpg

### Sports & Outdoors

- yoga-mat-1.jpg
- yoga-mat-2.jpg
- yoga-mat-3.jpg
- dumbbells-1.jpg
- dumbbells-2.jpg
- tent-1.jpg
- tent-2.jpg
- tent-3.jpg

### Books & Media

- book-programming-1.jpg
- book-programming-2.jpg

### Fashion

- backpack-1.jpg
- backpack-2.jpg
- backpack-3.jpg
- running-shoes-1.jpg
- running-shoes-2.jpg
- running-shoes-3.jpg

**Total: 33 images**
