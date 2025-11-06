# Frontend Project Structure

This project follows a feature-oriented architecture inspired by [bulletproof-react](https://github.com/alan2207/bulletproof-react).

## Directory Structure

```
src/
├── app/                    # Application layer
│   ├── routes/            # Route components (pages)
│   │   ├── landing.tsx    # Landing/home page
│   │   ├── login.tsx      # Login page
│   │   ├── products.tsx   # Products listing page
│   │   └── cart.tsx       # Shopping cart page
│   ├── index.tsx          # Main app component
│   ├── provider.tsx       # Global providers (React Query, etc.)
│   └── router.tsx         # Application routing configuration
│
├── features/              # Feature-based modules
│   ├── auth/              # Authentication feature
│   │   ├── api/           # Auth API calls
│   │   ├── components/    # Auth-specific components
│   │   └── types/         # Auth type definitions
│   ├── products/          # Products/catalog feature
│   │   ├── api/           # Product API calls
│   │   ├── components/    # Product-specific components
│   │   └── types/         # Product type definitions
│   └── cart/              # Shopping cart feature
│       ├── api/           # Cart API calls
│       ├── components/    # Cart-specific components
│       └── types/         # Cart type definitions
│
├── components/            # Shared components
│   └── layouts/           # Layout components
│       ├── header.tsx
│       ├── footer.tsx
│       └── dashboard-layout.tsx
│
├── lib/                   # Reusable libraries (API client, etc.)
├── hooks/                 # Shared custom hooks
├── config/                # Global configuration
│   └── paths.ts           # Route path definitions
├── types/                 # Shared TypeScript types
├── utils/                 # Utility functions
└── assets/                # Static assets (images, fonts, etc.)
```

## Key Principles

### Feature-Oriented Organization

- **Features are isolated**: Each feature contains its own components, API calls, types, and logic
- **No cross-feature imports**: Features should not import from each other directly
- **Compose at the app level**: Features are composed together in the `app/` directory

### Unidirectional Architecture

```
shared (components, hooks, utils)
  ↓
features (auth, products, cart)
  ↓
app (routes, router)
```

- Shared modules can be used anywhere
- Features can only import from shared modules
- App layer can import from features and shared modules

### Path Aliases

The project uses path aliases for cleaner imports:

```typescript
import { LoginForm } from "@/features/auth/components/login-form";
import { paths } from "@/config/paths";
import { DashboardLayout } from "@/components/layouts/dashboard-layout";
```

All imports from the `src/` directory use the `@/` prefix.

## Adding a New Feature

1. Create a new folder in `src/features/`
2. Add subdirectories as needed:

   - `api/` - API request functions
   - `components/` - Feature-specific components
   - `types/` - TypeScript type definitions
   - `hooks/` - Feature-specific hooks (optional)
   - `stores/` - Feature-specific state (optional)
   - `utils/` - Feature-specific utilities (optional)

3. Create route components in `src/app/routes/`
4. Add routes to `src/app/router.tsx`
5. Add path definitions to `src/config/paths.ts`

## Example: Creating a New Feature

```typescript
// 1. Create feature structure
src/features/orders/
  ├── api/
  │   └── get-orders.ts
  ├── components/
  │   └── order-list.tsx
  └── types/
      └── index.ts

// 2. Create route
// src/app/routes/orders.tsx
import { OrderList } from "@/features/orders/components/order-list";

export const OrdersRoute = () => {
  return (
    <div>
      <h1>My Orders</h1>
      <OrderList />
    </div>
  );
};

// 3. Add to router
// src/app/router.tsx
import { OrdersRoute } from "./routes/orders";

// Add route to Routes component
<Route path="/orders" element={<OrdersRoute />} />

// 4. Add to paths config
// src/config/paths.ts
export const paths = {
  // ... existing paths
  orders: {
    getHref: () => "/orders",
  },
};
```

## Benefits of This Structure

1. **Scalability**: Easy to add new features without affecting existing code
2. **Maintainability**: Clear separation of concerns
3. **Collaboration**: Multiple developers can work on different features simultaneously
4. **Testability**: Features can be tested in isolation
5. **Reusability**: Shared components and utilities are easily accessible
6. **Code Organization**: Related code is colocated

## Migration Notes

The project has been restructured from a traditional pages/components structure to this feature-oriented approach:

- Old `pages/` directory → `app/routes/`
- Old top-level components → `components/layouts/`
- Page-specific logic → Feature modules in `features/`
- New `app/` layer for application composition
