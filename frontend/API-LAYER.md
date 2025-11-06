# API Layer Documentation

This document describes the API layer architecture using React Query, Axios, and Yup validation.

## Architecture

### Shared Axios Instance

Located in `src/lib/api-client.ts`, this provides a configured Axios instance with:

- Base URL: `/api`
- Credentials enabled (`withCredentials: true`) for cookie-based authentication
- Global request/response interceptors for error handling

```typescript
import { api } from "@/lib/api-client";
```

### API Request Pattern

Each API endpoint follows this pattern:

1. **Schema Definition** (for mutations with input)
2. **Type Inference** from schema
3. **API Function** using Axios
4. **React Query Hook** (mutation or query)

## Example: Login API

### 1. Define Schema and Types

```typescript
// src/features/auth/types/index.ts
import * as yup from "yup";

export const loginInputSchema = yup.object({
  email: yup.string().email().required(),
  password: yup.string().min(6).required(),
});

export type LoginInput = yup.InferType<typeof loginInputSchema>;
```

### 2. Create API Function

```typescript
// src/features/auth/api/login.ts
import { api } from "@/lib/api-client";
import { LoginInput, AuthResponse } from "../types";

export const loginUser = async (input: LoginInput): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>("/auth/login", input);
  return response.data;
};
```

### 3. Create React Query Hook

```typescript
import { useMutation } from "@tanstack/react-query";

export const useLogin = () => {
  return useMutation({
    mutationFn: loginUser,
    onSuccess: (data) => {
      // Handle success
    },
    onError: (error) => {
      // Handle error
    },
  });
};
```

### 4. Use in Component

```typescript
import { useLogin } from "@/features/auth/api/login";
import { loginInputSchema } from "@/features/auth/types";

const LoginForm = () => {
  const login = useLogin();

  const onSubmit = (data: LoginInput) => {
    login.mutate(data);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      {/* Form fields */}
      <button disabled={login.isPending}>
        {login.isPending ? "Signing in..." : "Sign In"}
      </button>
    </form>
  );
};
```

## Query Pattern (GET requests)

### Example: Get Products

```typescript
// src/features/products/api/get-products.ts
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api-client";
import { ProductsResponse } from "../types";

export const getProducts = async (): Promise<ProductsResponse> => {
  const response = await api.get<ProductsResponse>("/catalog/products");
  return response.data;
};

export const useProducts = () => {
  return useQuery({
    queryKey: ["products"],
    queryFn: getProducts,
  });
};
```

### Usage in Component

```typescript
const ProductsRoute = () => {
  const { data, isLoading, isError } = useProducts();

  if (isLoading) return <LoadingSpinner />;
  if (isError) return <ErrorMessage />;

  return <ProductList products={data?.products || []} />;
};
```

## Mutation Pattern (POST/PUT/DELETE)

### Example: Add to Cart

```typescript
// src/features/cart/api/cart.ts
import { useMutation, useQueryClient } from "@tanstack/react-query";
import * as yup from "yup";
import { api } from "@/lib/api-client";

// 1. Schema
export const addToCartInputSchema = yup.object({
  productId: yup.string().required(),
  quantity: yup.number().min(1).required(),
});

export type AddToCartInput = yup.InferType<typeof addToCartInputSchema>;

// 2. API Function
export const addToCart = async (input: AddToCartInput): Promise<Cart> => {
  const response = await api.post<Cart>("/cart/items", input);
  return response.data;
};

// 3. Hook with cache invalidation
export const useAddToCart = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: addToCart,
    onSuccess: () => {
      // Invalidate cart query to refetch
      queryClient.invalidateQueries({ queryKey: ["cart"] });
    },
  });
};
```

## Available API Hooks

### Auth Feature

- `useLogin()` - Login mutation

### Products Feature

- `useProducts()` - Get all products query
- `useProduct(id)` - Get single product query

### Cart Feature

- `useCart()` - Get cart query
- `useAddToCart()` - Add item to cart mutation
- `useUpdateCartItem()` - Update cart item quantity mutation
- `useRemoveFromCart()` - Remove item from cart mutation

## Best Practices

### 1. Always Define Schemas for Mutations

```typescript
export const createOrderInputSchema = yup.object({
  items: yup
    .array()
    .of(
      yup.object({
        productId: yup.string().required(),
        quantity: yup.number().min(1).required(),
      })
    )
    .required(),
  shippingAddress: yup.string().required(),
});

export type CreateOrderInput = yup.InferType<typeof createOrderInputSchema>;
```

### 2. Use Type Inference

Infer types from schemas instead of manually defining them:

```typescript
// ✅ Good - types are automatically inferred
export type LoginInput = yup.InferType<typeof loginInputSchema>;

// ❌ Avoid - manual type definition can get out of sync
export interface LoginInput {
  email: string;
  password: string;
}
```

### 3. Invalidate Queries After Mutations

```typescript
export const useDeleteProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
};
```

### 4. Handle Loading and Error States

```typescript
const { data, isLoading, isError, error } = useProducts();

if (isLoading) return <Spinner />;
if (isError) return <ErrorAlert message={error.message} />;
```

### 5. Use Query Keys Consistently

```typescript
// Feature-based keys
["products"] - all products
["products", id] - single product
["cart"] - current cart
["orders"] - all orders
["orders", id] - single order
```

## Error Handling

Global error handling is configured in the Axios interceptor:

```typescript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
    }
    return Promise.reject(error);
  }
);
```

Component-level error handling:

```typescript
const login = useLogin();

if (login.isError) {
  return <p>Login failed. Please try again.</p>;
}
```

## Form Integration with React Hook Form

```typescript
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";

const {
  register,
  handleSubmit,
  formState: { errors },
} = useForm<LoginInput>({
  resolver: yupResolver(loginInputSchema),
});
```

This validates forms using the same Yup schemas defined for API calls.
