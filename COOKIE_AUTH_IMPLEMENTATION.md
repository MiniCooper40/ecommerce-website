# Cookie-Based Authentication Implementation Summary

## What Changed

Added support for JWT delivery via HttpOnly cookies to simplify frontend authentication and improve security.

## Modified Files

### Security Service

1. **application.yml** - Added cookie configuration

   ```yaml
   jwt:
     cookie:
       name: jwt_token
       path: /
       http-only: true
       secure: false
       same-site: Lax
       max-age: 86400
   ```

2. **CookieUtil.java** (NEW) - Utility class for cookie management

   - Creates JWT cookies with proper security settings
   - Extracts JWT from request cookies
   - Creates delete cookies for logout

3. **AuthController.java** - Updated to set cookies on auth endpoints

   - `/auth/login` - Sets JWT cookie
   - `/auth/register` - Sets JWT cookie
   - `/auth/refresh` - Accepts cookie OR header, returns new cookie
   - `/auth/logout` (NEW) - Clears JWT cookie

4. **SecurityConfig.java** - Updated CORS to expose Set-Cookie header
   - Added `Set-Cookie` to exposed headers
   - Maintains `allowCredentials: true`

### Gateway

5. **CorsConfig.java** - Updated to expose Set-Cookie header
   - Added `Set-Cookie` to exposed headers
   - Ensures cookies flow through gateway

## New Features

### 1. Cookie-Based Authentication

- JWTs automatically sent via HttpOnly cookies
- No manual token management needed in frontend

### 2. Logout Endpoint

```http
POST /api/auth/logout
```

Clears the JWT cookie.

### 3. Dual-Mode Support

- Accepts JWT from cookies (preferred)
- Falls back to Authorization header (backward compatible)

## Frontend Usage

### Configure Axios

```typescript
const api = axios.create({
  baseURL: "/api",
  withCredentials: true, // Enable cookie support
});
```

### Login

```typescript
await api.post("/auth/login", { email, password });
// Cookie is set automatically - no token management needed!
```

### Make Requests

```typescript
const cart = await api.get("/cart");
// Cookie is sent automatically with every request
```

### Logout

```typescript
await api.post("/auth/logout");
// Cookie is cleared
```

## Security Benefits

1. **XSS Protection** - HttpOnly cookies can't be accessed by JavaScript
2. **CSRF Protection** - SameSite=Lax prevents cross-site attacks
3. **Automatic Expiration** - Max-Age enforces token lifetime
4. **Simplified Code** - No manual token storage/retrieval

## Backward Compatibility

✅ Still returns token in response body
✅ Still accepts Authorization header
✅ Existing integrations continue to work

## Production Checklist

Before deploying to production:

- [ ] Enable HTTPS
- [ ] Set `jwt.cookie.secure: true`
- [ ] Consider `same-site: Strict` for same-origin
- [ ] Configure specific allowed origins (not `*`)
- [ ] Test cookie behavior in production environment

## Documentation

See `backend/services/security-service/COOKIE_AUTHENTICATION.md` for complete documentation.
