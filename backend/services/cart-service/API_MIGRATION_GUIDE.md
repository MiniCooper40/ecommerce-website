# Cart Service API Migration Guide

## Overview

This guide helps frontend developers migrate to the new CQRS-based cart API.

## Breaking Changes

### 1. Add Item to Cart

#### OLD API

```http
POST /cart/items
Content-Type: application/json

{
  "productId": 123,
  "productName": "Product Name",
  "productPrice": 29.99,
  "productImageUrl": "https://...",
  "quantity": 2
}

Response: 200 OK
{
  "id": 456,
  "productId": 123,
  "productName": "Product Name",
  "productPrice": 29.99,
  "productImageUrl": "https://...",
  "quantity": 2,
  "createdAt": "2025-11-03T14:00:00",
  "updatedAt": "2025-11-03T14:00:00"
}
```

#### NEW API

```http
POST /cart/items
Content-Type: application/json

{
  "productId": 123,
  "quantity": 2
}

Response: 200 OK
456  // Returns just the cartItemId (Long)
```

**Frontend Changes Required**:

- Remove product details from request (productName, productPrice, productImageUrl)
- Response is now just the ID, not full object
- After adding, fetch the full cart with GET /cart to see updated cart

### 2. Get Cart (No Changes)

```http
GET /cart

Response: 200 OK
{
  "items": [
    {
      "id": 456,
      "productId": 123,
      "productName": "Product Name",  // Still returned!
      "productPrice": 29.99,          // Still returned!
      "productImageUrl": "https://...", // Still returned!
      "quantity": 2,
      "createdAt": "2025-11-03T14:00:00",
      "updatedAt": "2025-11-03T14:00:00"
    }
  ],
  "totalItems": 2,
  "subtotal": 59.98
}
```

**No changes needed** - Response format remains the same

### 3. Update Quantity (No Changes)

```http
PUT /cart/items/456/quantity?quantity=3

Response: 204 No Content
```

**No changes needed** - API unchanged

### 4. Remove Item (No Changes)

```http
DELETE /cart/items/456

Response: 204 No Content
```

**No changes needed** - API unchanged

### 5. Clear Cart (No Changes)

```http
DELETE /cart

Response: 204 No Content
```

**No changes needed** - API unchanged

### 6. Get Item Count (No Changes)

```http
GET /cart/count

Response: 200 OK
5  // Total quantity
```

**No changes needed** - API unchanged

## Frontend Migration Steps

### Step 1: Update Add to Cart Function

**Before**:

```javascript
async function addToCart(product, quantity) {
  const response = await fetch("/cart/items", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      productId: product.id,
      productName: product.name,
      productPrice: product.price,
      productImageUrl: product.imageUrl,
      quantity: quantity,
    }),
  });

  const cartItem = await response.json();
  return cartItem;
}
```

**After**:

```javascript
async function addToCart(productId, quantity) {
  // POST to add item
  const response = await fetch("/cart/items", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      productId: productId,
      quantity: quantity,
    }),
  });

  const cartItemId = await response.json();

  // Optionally fetch full cart to update UI
  const cart = await fetchCart();
  return { cartItemId, cart };
}
```

### Step 2: Consider Event-Driven Updates

Since the backend is now event-driven, there may be a small delay between adding an item and the product details being available. Consider:

```javascript
async function addToCart(productId, quantity) {
  const cartItemId = await addCartItem(productId, quantity);

  // Poll for cart update (with timeout)
  let attempts = 0;
  while (attempts < 5) {
    await sleep(200); // Wait 200ms
    const cart = await fetchCart();
    const item = cart.items.find((i) => i.id === cartItemId);

    if (item && item.productName) {
      // Product details loaded!
      return { cartItemId, cart };
    }
    attempts++;
  }

  // Fallback - return what we have
  return { cartItemId };
}
```

### Step 3: Handle Optimistic UI Updates

**Option A**: Optimistic with product data from context

```javascript
async function addToCart(product, quantity) {
  // 1. Immediately update UI with product data you already have
  updateCartUI({
    id: "temp-" + Date.now(),
    productId: product.id,
    productName: product.name,
    productPrice: product.price,
    productImageUrl: product.imageUrl,
    quantity: quantity,
    isLoading: true,
  });

  // 2. Make API call
  const cartItemId = await addCartItem(product.id, quantity);

  // 3. Fetch actual cart (eventually consistent)
  setTimeout(async () => {
    const cart = await fetchCart();
    updateCartUI(cart.items);
  }, 500);
}
```

**Option B**: Loading state

```javascript
async function addToCart(productId, quantity) {
  // 1. Show loading indicator
  setCartLoading(true);

  // 2. Add item
  await addCartItem(productId, quantity);

  // 3. Fetch full cart
  const cart = await fetchCart();

  // 4. Update UI
  setCartLoading(false);
  updateCartUI(cart.items);
}
```

## Recommended Approach

**Best practice for add to cart**:

```javascript
async function addToCart(product, quantity) {
  try {
    // 1. Validate locally
    if (quantity < 1) throw new Error("Invalid quantity");

    // 2. Optimistic update (using product data you already have)
    const tempItem = {
      id: "temp-" + Date.now(),
      ...product,
      quantity,
      isOptimistic: true,
    };
    addItemToCartUI(tempItem);

    // 3. Call API
    const cartItemId = await fetch("/cart/items", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        productId: product.id,
        quantity,
      }),
    }).then((r) => r.json());

    // 4. Wait a moment for event processing
    await sleep(300);

    // 5. Fetch authoritative cart data
    const cart = await fetch("/cart", {
      headers: { Authorization: `Bearer ${token}` },
    }).then((r) => r.json());

    // 6. Replace optimistic update with real data
    replaceCartUI(cart.items);

    return { success: true, cartItemId };
  } catch (error) {
    // 7. Rollback optimistic update on error
    removeOptimisticItemFromUI(tempItem.id);
    throw error;
  }
}
```

## Notes

1. **Product Details**: You don't need to send product details anymore - the backend fetches them from the catalog service
2. **Eventually Consistent**: The cart view is updated asynchronously via events. Usually takes < 500ms
3. **Error Handling**: If catalog service is down, the cart item is still created but may not have product details immediately
4. **Caching**: Product details are cached in Redis for 1 hour, so subsequent adds are faster

## Testing

Test these scenarios:

1. Add item and immediately fetch cart
2. Add item while catalog service is slow (should still work)
3. Add multiple items rapidly
4. Product price changes in catalog (should update in cart within cache TTL)

## Support

If you encounter issues:

- Check browser console for errors
- Verify JWT token is valid
- Ensure productId exists in catalog service
- Check network tab for response codes
