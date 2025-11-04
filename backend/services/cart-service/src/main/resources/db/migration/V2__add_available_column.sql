-- Migration script to add available column to cart_item_view
-- This column tracks whether the product is still available for purchase

-- Add available column with default value of true
ALTER TABLE cart_item_view 
ADD COLUMN IF NOT EXISTS available BOOLEAN NOT NULL DEFAULT true;

-- Add index on available column for efficient filtering
CREATE INDEX IF NOT EXISTS idx_cart_view_available ON cart_item_view(available);

-- Add comment explaining the column
COMMENT ON COLUMN cart_item_view.available IS 'Indicates if the product is still available for purchase. Set to false when product is deleted.';
