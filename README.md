# 🛒 E-Commerce Microservices Application

A modern full-stack e-commerce application built with React TypeScript frontend and Spring Boot microservices backend.

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Frontend](https://img.shields.io/badge/frontend-React%2018%20%2B%20TypeScript-blue)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.2-green)
![License](https://img.shields.io/badge/license-MIT-blue)

## 🏗️ Architecture

### Frontend Stack

- **Framework**: React 18 with TypeScript
- **Styling**: Tailwind CSS
- **Build Tool**: Vite (fast development & building)
- **State Management**: React Query for server state
- **Routing**: React Router v6
- **Package Manager**: npm

### Backend Microservices

- **🔄 API Gateway**: Spring Cloud Gateway with JWT authentication
- **🔐 Security Service**: User authentication and authorization
- **📦 Catalog Service**: Product management and inventory
- **🛒 Cart Service**: Shopping cart with Redis sessions
- **📋 Order Service**: Order processing and management

### Infrastructure

- **Database**: H2 (development) → PostgreSQL/MySQL (production)
- **Caching**: Redis for cart sessions and performance
- **Containerization**: Docker & Docker Compose
- **Authentication**: JWT tokens with HMAC-256 signing
- **Build Tools**: Maven for Java, Vite for frontend

## 📁 Project Structure

```
ecommerce-website/
├── 🖥️ frontend/                 # React TypeScript SPA
│   ├── src/
│   │   ├── components/          # Reusable UI components
│   │   ├── pages/               # Route-level components
│   │   ├── hooks/               # Custom React hooks
│   │   ├── utils/               # Helper functions
│   │   └── types/               # TypeScript definitions
│   ├── Dockerfile               # Frontend container
│   └── package.json
├── ⚙️ backend/
│   ├── gateway/                 # 🌐 API Gateway (Port 8080)
│   ├── security-service/        # 🔐 Auth Service (Port 8081)
│   ├── catalog-service/         # 📦 Products (Port 8082)
│   ├── cart-service/            # 🛒 Shopping Cart (Port 8083)
│   └── order-service/           # 📋 Orders (Port 8084)
├── 🐋 docker-compose.yml        # Multi-container orchestration
├── 🧪 test_system.py           # Integration test suite
└── 📖 README.md
```

## ✨ Features

### ✅ Implemented

- ✅ **Modern React Frontend** - TypeScript, Tailwind CSS, responsive design
- ✅ **API Gateway** - Centralized routing with JWT authentication
- ✅ **User Authentication** - Register, login, role-based authorization
- ✅ **JWT Security** - Modern JJWT 0.12.3 implementation
- ✅ **Shopping Cart** - Redis-backed session management
- ✅ **Microservices Architecture** - Scalable, maintainable backend
- ✅ **Docker Support** - Complete containerization
- ✅ **Role-Based Access** - USER, ADMIN, MANAGER permissions

### 🔄 In Development

- 🔄 **Product Catalog** - Search, filtering, categories
- 🔄 **Order Processing** - Complete checkout workflow
- 🔄 **Payment Integration** - Stripe/PayPal support
- 🔄 **User Profiles** - Account management, preferences
- 🔄 **Admin Dashboard** - Management interface
- 🔄 **Email Notifications** - Order confirmations, updates

## 🚀 Quick Start

### Prerequisites

- ✅ **Node.js** 18+ (for frontend)
- ✅ **Java** 17+ (for backend services)
- ✅ **Maven** 3.6+ (build tool)
- ✅ **Redis** (for cart service)
- ✅ **Docker** & Docker Compose (optional but recommended)

### 🏃‍♂️ Development Setup

1. **📥 Clone Repository**

   ```bash
   git clone <repository-url>
   cd ecommerce-website
   ```

2. **🔧 Start Backend Services**

   ```bash
   # 🔐 Security Service (Port 8081)
   cd backend/security-service
   mvn spring-boot:run &

   # 🌐 Gateway Service (Port 8080)
   cd ../gateway
   mvn spring-boot:run &

   # 🛒 Cart Service (Port 8083)
   cd ../cart-service
   mvn spring-boot:run &
   ```

3. **🖥️ Start Frontend**

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **🌍 Access Application**
   - **Frontend**: http://localhost:5173
   - **API Gateway**: http://localhost:8080
   - **Security Service**: http://localhost:8081
   - **H2 Database Console**: http://localhost:8081/h2-console

### 🐋 Docker Setup (Recommended)

```bash
# 🚀 Start everything with one command
docker-compose up --build

# 🌍 Access points:
# Frontend: http://localhost:3000
# API Gateway: http://localhost:8080
```

## 📚 API Documentation

### 🔐 Authentication Endpoints

```http
POST /api/auth/register    # Create new user account
POST /api/auth/login       # User login (returns JWT)
GET  /api/auth/profile     # Get user profile (requires auth)
```

### 🌐 Gateway Routes

```http
/security/**  → Security Service (8081)
/catalog/**   → Catalog Service (8082)
/cart/**      → Cart Service (8083)
/order/**     → Order Service (8084)
```

### 🔒 Role-Based Access Control

- **🌍 Public**: Registration, login, product browsing
- **👤 USER**: Cart management, personal orders, profile
- **👨‍💼 MANAGER**: Product management, inventory control
- **👑 ADMIN**: All endpoints, user management, system settings

### 💾 Example API Calls

**Register User:**

```bash
curl -X POST http://localhost:8080/security/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Login:**

```bash
curl -X POST http://localhost:8080/security/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

## ⚙️ Configuration

### 🔑 Environment Variables

```bash
# JWT Configuration
JWT_SECRET=your-super-secret-key-here-min-32-chars
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# Database Settings
SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=password

# Redis Configuration
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# Application Profiles
SPRING_PROFILES_ACTIVE=development  # or production
```

### 🔧 Application Profiles

- **development**: H2 database, debug logging, H2 console enabled
- **production**: PostgreSQL/MySQL, optimized settings, security hardened

## 🔒 Security Features

### 🎯 JWT Implementation

- **Algorithm**: HMAC-SHA256 signing
- **Expiration**: 24-hour token lifetime
- **Claims**: User ID, email, roles, issued/expiry times
- **Validation**: Automatic token verification in gateway

### 🔐 Password Security

- **Hashing**: BCrypt with configurable salt rounds
- **Validation**: Minimum length and complexity requirements
- **Storage**: Never store plain text passwords

### 🛡️ API Security

- **CORS**: Configurable cross-origin resource sharing
- **Validation**: Input validation and sanitization
- **Error Handling**: Secure error responses without data leakage
- **Rate Limiting**: (Planned) Request throttling protection

## 🧪 Testing

### 🚀 Quick Test

```bash
# 🧪 Run the integration test suite
python test_system.py

# 🔍 Test individual services
cd backend/security-service && mvn test
cd frontend && npm test
```

### 🎯 Manual Testing Flow

1. **📝 Register** new user at `/register`
2. **🔑 Login** with credentials
3. **🛒 Add items** to cart
4. **📋 Create order** from cart
5. **👑 Test admin** features (if admin role)

## 🛠️ Development

### 📋 Code Standards

- **Frontend**: ESLint + Prettier, TypeScript strict mode
- **Backend**: Spring Boot conventions, SonarLint integration
- **Git**: Conventional commits, feature branch workflow

### 📊 Monitoring

- **Health Checks**: Spring Boot Actuator endpoints
- **Metrics**: Application performance monitoring
- **Logging**: Structured logging with correlation IDs

### 🗄️ Database

- **Development**: H2 in-memory with web console
- **Production**: PostgreSQL with connection pooling
- **Migrations**: Flyway for schema versioning

## 🚀 Deployment

### 🏭 Production Checklist

- [ ] 🔑 Update JWT secret (minimum 32 characters)
- [ ] 🗄️ Configure production database
- [ ] 🚀 Set up Redis cluster/sentinel
- [ ] 🔒 Configure HTTPS/TLS certificates
- [ ] 📊 Set up monitoring and alerting
- [ ] 💾 Configure backup and disaster recovery
- [ ] 🔧 Optimize JVM settings for production

### ☸️ Kubernetes (Planned)

- Helm charts for easy deployment
- ConfigMaps for environment-specific configuration
- Secrets management for sensitive data
- Horizontal Pod Autoscaling (HPA)

## 🔧 Troubleshooting

### 🚨 Common Issues

1. **❌ JWT "parserBuilder is not a method"**

   - ✅ **Solution**: Updated to JJWT 0.12.3 with modern API

2. **🌐 CORS errors in browser**

   - ✅ **Check**: Gateway CORS configuration
   - ✅ **Verify**: Frontend URL matches allowed origins

3. **🔌 Service connection issues**

   - ✅ **Ensure**: All services running on correct ports
   - ✅ **Check**: Gateway route configuration in application.yml

4. **🗄️ Database connection errors**
   - ✅ **Development**: Verify H2 console access
   - ✅ **Production**: Check database connectivity and credentials

### 📍 Health Check URLs

```bash
# Service health endpoints
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Security
curl http://localhost:8083/actuator/health  # Cart
```

## 💻 Tech Stack

### Frontend Technologies

![React](https://img.shields.io/badge/React-18-blue?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue?logo=typescript)
![Tailwind](https://img.shields.io/badge/Tailwind-3.0-blue?logo=tailwindcss)
![Vite](https://img.shields.io/badge/Vite-4.0-purple?logo=vite)

### Backend Technologies

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.0-green?logo=springsecurity)
![JWT](https://img.shields.io/badge/JWT-HMAC256-orange?logo=jsonwebtokens)
![Redis](https://img.shields.io/badge/Redis-7.0-red?logo=redis)

### DevOps & Tools

![Docker](https://img.shields.io/badge/Docker-24.0-blue?logo=docker)
![Maven](https://img.shields.io/badge/Maven-3.9-red?logo=apachemaven)
![H2](https://img.shields.io/badge/H2-Database-lightblue)

## 🤝 Contributing

1. **🍴 Fork** the repository
2. **🌿 Create** feature branch (`git checkout -b feature/amazing-feature`)
3. **💻 Code** your changes
4. **✅ Add** tests for new functionality
5. **📤 Submit** pull request with clear description

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

Need help? Here's how to get support:

- 🐛 **Bug Reports**: Create an issue with detailed reproduction steps
- 💡 **Feature Requests**: Open an issue with your enhancement idea
- 📖 **Documentation**: Check this README and code comments
- 💬 **Questions**: Use GitHub Discussions for general questions

## 🎯 Roadmap

### 🗓️ Upcoming Features

- **Q1 2024**: Complete product catalog with search
- **Q2 2024**: Payment integration (Stripe/PayPal)
- **Q3 2024**: Admin dashboard and analytics
- **Q4 2024**: Mobile app (React Native)

---

<div align="center">

**⭐ Star this repo if it helped you! ⭐**

Built with ❤️ using modern technologies and best practices

_Made by developers, for developers_ 🚀

</div>
