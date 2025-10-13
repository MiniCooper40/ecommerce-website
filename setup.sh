#!/bin/bash

# Development setup script for Ecommerce Monorepo

echo "🚀 Setting up Ecommerce Development Environment"

# Install frontend dependencies
echo "📦 Installing frontend dependencies..."
cd frontend
npm install
cd ..

# Create .env files from examples if they don't exist
echo "🔧 Setting up environment files..."

if [ ! -f frontend/.env ]; then
    cp frontend/.env.example frontend/.env
    echo "✅ Created frontend/.env"
fi

if [ ! -f backend/gateway/.env ]; then
    cp backend/gateway/.env.example backend/gateway/.env
    echo "✅ Created backend/gateway/.env"
fi

if [ ! -f backend/security-service/.env ]; then
    cp backend/security-service/.env.example backend/security-service/.env
    echo "✅ Created backend/security-service/.env"
fi

if [ ! -f backend/catalog-service/.env ]; then
    cp backend/catalog-service/.env.example backend/catalog-service/.env
    echo "✅ Created backend/catalog-service/.env"
fi

if [ ! -f backend/order-service/.env ]; then
    cp backend/order-service/.env.example backend/order-service/.env
    echo "✅ Created backend/order-service/.env"
fi

if [ ! -f backend/cart-service/.env ]; then
    cp backend/cart-service/.env.example backend/cart-service/.env
    echo "✅ Created backend/cart-service/.env"
fi

echo ""
echo "✨ Setup complete! You can now:"
echo ""
echo "🖥️  Start frontend development server:"
echo "   npm run dev:frontend"
echo ""
echo "🔧 Start backend services:"
echo "   npm run start:backend"
echo ""
echo "🌐 Start everything:"
echo "   npm run start:all"
echo ""
echo "📊 View services:"
echo "   Frontend: http://localhost:3000"
echo "   Gateway: http://localhost:8080"
echo "   Security Service: http://localhost:8081"
echo "   Catalog Service: http://localhost:8082"
echo "   Order Service: http://localhost:8083"
echo "   Cart Service: http://localhost:8084"