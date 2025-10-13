# Ecommerce Application

A modern ecommerce application built with React frontend and Spring Boot microservices.

## Architecture

### Frontend

- **React + TypeScript + Tailwind CSS** - Modern, responsive web application
- **Vite** - Fast build tool and development server

### Backend Microservices

- **Gateway Service** - API Gateway with request validation and principal injection
- **Security Service** - Authentication and authorization
- **Catalog Service** - Product catalog management
- **Order Service** - Order processing and management
- **Cart Service** - Shopping cart management

## Project Structure

```
├── frontend/                 # React application
├── backend/
│   ├── gateway/             # API Gateway
│   ├── security-service/    # Authentication & Authorization
│   ├── catalog-service/     # Product catalog
│   ├── order-service/       # Order management
│   └── cart-service/        # Shopping cart management
├── docker-compose.yml       # Local development environment
└── README.md
```

## Getting Started

### Prerequisites

- Node.js 18+
- Java 17+
- Docker & Docker Compose
- Maven 3.6+ (or use included wrapper)

### Quick Setup

**Windows (PowerShell):**

```powershell
.\setup.ps1
```

**Linux/Mac:**

```bash
chmod +x setup.sh
./setup.sh
```

### Manual Setup

1. **Install Dependencies**

   ```bash
   npm run install:frontend
   ```

2. **Create Environment Files**
   Copy `.env.example` files to `.env` in each service directory:
   ```bash
   cp frontend/.env.example frontend/.env
   cp backend/gateway/.env.example backend/gateway/.env
   cp backend/security-service/.env.example backend/security-service/.env
   cp backend/catalog-service/.env.example backend/catalog-service/.env
   cp backend/order-service/.env.example backend/order-service/.env
   ```

### Development Options

**Option 1: Full Stack Development (Recommended)**

```bash
npm run start:all
```

**Option 2: Frontend + Backend Services**

```bash
# Terminal 1: Start backend services
npm run start:backend

# Terminal 2: Start frontend development server
npm run dev:frontend
```

**Option 3: Individual Service Development**

```bash
# Start specific backend services
cd backend/[service-name]
./mvnw spring-boot:run

# Start frontend
npm run dev:frontend
```

### Available Scripts

- `npm run dev:frontend` - Start frontend development server
- `npm run build:frontend` - Build frontend for production
- `npm run start:backend` - Start all backend services via Docker
- `npm run start:all` - Start all services (frontend + backend)
- `npm run stop` - Stop all Docker services
- `npm run clean` - Stop services and remove volumes
- `npm run logs` - View logs from all services

## Service URLs

Once services are running:

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Security Service**: http://localhost:8081
- **Catalog Service**: http://localhost:8082
- **Order Service**: http://localhost:8083
- **Cart Service**: http://localhost:8084

## API Endpoints

### Authentication (via Gateway)

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh token
- `GET /api/auth/validate` - Validate token

### Catalog (via Gateway)

- `GET /api/catalog/products` - Get all products (paginated)
- `GET /api/catalog/products/{id}` - Get product by ID
- `GET /api/catalog/products/category/{category}` - Get products by category
- `POST /api/catalog/products` - Create product (admin)
- `PUT /api/catalog/products/{id}` - Update product (admin)
- `DELETE /api/catalog/products/{id}` - Delete product (admin)

### Orders (via Gateway) - Requires Authentication

- `GET /api/orders` - Get user orders
- `GET /api/orders/{id}` - Get specific order
- `POST /api/orders` - Create new order
- `PUT /api/orders/{id}/cancel` - Cancel order

### Cart (via Gateway) - Requires Authentication

- `GET /api/cart` - Get user's cart with summary
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{itemId}` - Update cart item
- `DELETE /api/cart/items/{itemId}` - Remove item from cart
- `PUT /api/cart/items/{itemId}/quantity` - Update item quantity
- `DELETE /api/cart` - Clear entire cart
- `GET /api/cart/count` - Get total items count in cart

## Development Notes

### Architecture Decisions

- **Gateway Pattern**: All external requests go through the API Gateway
- **JWT Authentication**: Stateless authentication using JSON Web Tokens
- **Principal Injection**: Gateway extracts user info from JWT and passes to services via headers
- **H2 Database**: In-memory database for development (easily switchable to PostgreSQL/MySQL)
- **Modern Frontend**: React 18 + TypeScript + Tailwind CSS + Vite for fast development

### Security Features

- JWT-based authentication
- Request validation at gateway level
- CORS configuration for cross-origin requests
- Principal injection for user context

### Development Features

- Hot reload for frontend development
- Automatic restart for Spring Boot services
- Health checks and metrics endpoints
- Centralized logging
- Docker containerization for easy deployment
