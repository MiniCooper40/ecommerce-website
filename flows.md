# Interaction flows

## Cart

**Add item**

- Frontend {productId, quantity} -> Cart
- Cart {productId} <-> Product {price, imageUrl}

## Catalog

**Update item**

- Frontend (Admin) {productId, updatedProduct} -> Catalog
- Emit ItemUpdated{productId, updatedProduct}

**Delete item**

- Frontend (Admin) {productId} -> Catalog
- Emit ItemDeleted{productId}

## Order

**Checkout**

- Frontend {currency, items: {productId, quantity, price}, cartId, addressId} -> Order
- Order{cartId} <-> Cart{items} // Verify cart is up to date if cart checkout as opposed to directly paying for a single item
- Order{items} <-> Catalog // Verify quantities and prices
- Order emit OrderPlaced{cartId, items}
