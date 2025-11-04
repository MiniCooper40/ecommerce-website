-- Migration script for CQRS refactoring of cart service
-- Run this script to update the database schema

-- Step 1: Create backup of existing cart_items table
CREATE TABLE IF NOT EXISTS cart_items_backup AS SELECT * FROM cart_items;

-- Step 2: Create new cart_item_view table for denormalized data
CREATE TABLE IF NOT EXISTS cart_item_view (
    id BIGSERIAL PRIMARY KEY,
    cart_item_id BIGINT NOT NULL UNIQUE,
    cart_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    product_description TEXT,
    product_price DECIMAL(10, 2),
    product_image_url TEXT,
    product_category VARCHAR(100),
    product_active BOOLEAN,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Step 3: Create indexes for cart_item_view
CREATE INDEX IF NOT EXISTS idx_cart_view_user_id ON cart_item_view(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_view_cart_id ON cart_item_view(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_view_product_id ON cart_item_view(product_id);

-- Step 4: Drop old columns from cart_items table
ALTER TABLE cart_items DROP COLUMN IF EXISTS product_name;
ALTER TABLE cart_items DROP COLUMN IF EXISTS product_price;
ALTER TABLE cart_items DROP COLUMN IF EXISTS product_image_url;

-- Step 5: Add cart_id column to cart_items if not exists
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS cart_id VARCHAR(255);

-- Step 6: Generate cart_id for existing records
UPDATE cart_items SET cart_id = gen_random_uuid()::text WHERE cart_id IS NULL;

-- Step 7: Make cart_id NOT NULL
ALTER TABLE cart_items ALTER COLUMN cart_id SET NOT NULL;

-- Step 8: Create indexes for cart_items
CREATE INDEX IF NOT EXISTS idx_cart_items_user_id ON cart_items(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_user_product ON cart_items(user_id, product_id);

-- Step 9: Migrate data to cart_item_view (this will be populated by events in the new system)
-- For initial migration, you may need to manually populate or use a data migration job
-- that fetches product details and populates the view

COMMENT ON TABLE cart_items IS 'Write model - Minimal cart item data for CQRS';
COMMENT ON TABLE cart_item_view IS 'Read model - Denormalized cart item with product details for CQRS';
