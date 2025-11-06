# Cookie-Based Authentication

The security service now supports JWT token delivery via HttpOnly cookies, providing enhanced security and simplified frontend implementation.

## Overview

The service now issues JWTs as **HttpOnly cookies** in addition to the response body. This means:

- Frontend no longer needs to manually manage JWT tokens
- Tokens are automatically included in requests via cookies
- Protection against XSS attacks (HttpOnly cookies can't be accessed by JavaScript)
- Simplified axios configuration with `withCredentials: true`

## Configuration

Cookie settings are configured in `application.yml`:

```yaml
jwt:
  cookie:
    name: jwt_token # Cookie name
    path: / # Cookie path
    http-only: true # Prevents JavaScript access
    secure: false # Set to true in production (HTTPS only)
    same-site: Lax # CSRF protection
    max-age: 86400 # 24 hours
```

## Backend Changes

### 1. Cookie Utility Class

Created `CookieUtil` to manage cookie operations:

- `createJwtCookie(token)` - Creates cookie with JWT
- `createDeleteCookie()` - Creates cookie for logout
- `getJwtFromCookies(request)` - Extracts JWT from cookies

### 2. Updated Auth Endpoints

All authentication endpoints now set cookies:

#### Register & Login

```http
POST /api/auth/register
POST /api/auth/login

Response Headers:
Set-Cookie: jwt_token=eyJhbGc...; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400
```

#### Refresh Token

```http
POST /api/auth/refresh

Request:
- Can use Cookie: jwt_token=... (preferred)
- Or Authorization: Bearer ... (fallback)

Response Headers:
Set-Cookie: jwt_token=newToken...; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400
```

#### Logout

```http
POST /api/auth/logout

Response Headers:
Set-Cookie: jwt_token=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0
```

### 3. CORS Configuration

Updated to expose `Set-Cookie` header:

```java
configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
configuration.setAllowCredentials(true);
```

## Frontend Integration

### Axios Configuration

Configure axios to send cookies automatically:

```typescript
import axios from "axios";

const api = axios.create({
  baseURL: "/api",
  withCredentials: true, // This sends cookies automatically!
});

export default api;
```

### Login Example

```typescript
// Login - cookie is set automatically
const response = await api.post("/auth/login", {
  email: "user@example.com",
  password: "password123",
});

// No need to manually store token!
// The JWT is now in an HttpOnly cookie
```

### Making Authenticated Requests

```typescript
// Just make the request - cookie is sent automatically
const cart = await api.get("/cart");
const products = await api.get("/catalog/products");
```

### Logout

```typescript
// Clear the cookie
await api.post("/auth/logout");
```

## Migration Guide

### Before (Manual Token Management)

```typescript
// Store token
localStorage.setItem("token", response.data.token);

// Add to every request
const config = {
  headers: {
    Authorization: `Bearer ${localStorage.getItem("token")}`,
  },
};
const response = await axios.get("/cart", config);
```

### After (Cookie-Based)

```typescript
// Login - that's it!
await api.post("/auth/login", credentials);

// All subsequent requests include cookie automatically
const response = await api.get("/cart");
```

## Backward Compatibility

The service maintains backward compatibility:

- Still returns token in response body
- Still accepts `Authorization` header
- Cookies are **additive**, not replacing existing functionality

## Security Features

1. **HttpOnly**: Prevents JavaScript access to token (XSS protection)
2. **SameSite=Lax**: Protects against CSRF attacks
3. **Secure flag**: Set to true in production for HTTPS-only
4. **Path**: Scoped to root path
5. **Max-Age**: Automatic expiration

## Production Considerations

For production deployment:

1. **Enable HTTPS** and set `jwt.cookie.secure: true`
2. **Update SameSite** to `Strict` if your frontend is same-origin
3. **Configure allowed origins** specifically (not `*`)
4. **Set proper cookie domain** if using subdomains

Example production config:

```yaml
jwt:
  cookie:
    secure: true
    same-site: Strict
```

## Testing with cURL

```bash
# Login and save cookie
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Use cookie in subsequent request
curl -b cookies.txt http://localhost:8080/api/cart

# Logout
curl -b cookies.txt -c cookies.txt -X POST http://localhost:8080/api/auth/logout
```

## Troubleshooting

### Cookies not being set

- Check CORS `allowCredentials: true` is set
- Verify `withCredentials: true` in frontend
- Check browser console for CORS errors

### Cookies not being sent

- Ensure `withCredentials: true` in axios config
- Check cookie domain/path matches request
- Verify cookie hasn't expired

### SameSite warnings

- Use `Lax` for development (allows some cross-site)
- Use `Strict` for production same-origin setups
- Check browser compatibility
