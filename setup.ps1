# Development setup script for Ecommerce Monorepo

Write-Host "🚀 Setting up Ecommerce Development Environment" -ForegroundColor Green

# Install frontend dependencies
Write-Host "📦 Installing frontend dependencies..." -ForegroundColor Yellow
Set-Location frontend
npm install
Set-Location ..

# Create .env files from examples if they don't exist
Write-Host "🔧 Setting up environment files..." -ForegroundColor Yellow

if (!(Test-Path "frontend\.env")) {
    Copy-Item "frontend\.env.example" "frontend\.env"
    Write-Host "✅ Created frontend\.env" -ForegroundColor Green
}

if (!(Test-Path "backend\gateway\.env")) {
    Copy-Item "backend\gateway\.env.example" "backend\gateway\.env"
    Write-Host "✅ Created backend\gateway\.env" -ForegroundColor Green
}

if (!(Test-Path "backend\security-service\.env")) {
    Copy-Item "backend\security-service\.env.example" "backend\security-service\.env"
    Write-Host "✅ Created backend\security-service\.env" -ForegroundColor Green
}

if (!(Test-Path "backend\catalog-service\.env")) {
    Copy-Item "backend\catalog-service\.env.example" "backend\catalog-service\.env"
    Write-Host "✅ Created backend\catalog-service\.env" -ForegroundColor Green
}

if (!(Test-Path "backend\order-service\.env")) {
    Copy-Item "backend\order-service\.env.example" "backend\order-service\.env"
    Write-Host "✅ Created backend\order-service\.env" -ForegroundColor Green
}

if (!(Test-Path "backend\cart-service\.env")) {
    Copy-Item "backend\cart-service\.env.example" "backend\cart-service\.env"
    Write-Host "✅ Created backend\cart-service\.env" -ForegroundColor Green
}

Write-Host ""
Write-Host "✨ Setup complete! You can now:" -ForegroundColor Green
Write-Host ""
Write-Host "🖥️  Start frontend development server:" -ForegroundColor Cyan
Write-Host "   npm run dev:frontend"
Write-Host ""
Write-Host "🔧 Start backend services:" -ForegroundColor Cyan
Write-Host "   npm run start:backend"
Write-Host ""
Write-Host "🌐 Start everything:" -ForegroundColor Cyan
Write-Host "   npm run start:all"
Write-Host ""
Write-Host "📊 View services:" -ForegroundColor Cyan
Write-Host "   Frontend: http://localhost:3000"
Write-Host "   Gateway: http://localhost:8080"
Write-Host "   Security Service: http://localhost:8081"
Write-Host "   Catalog Service: http://localhost:8082"
Write-Host "   Order Service: http://localhost:8083"
Write-Host "   Cart Service: http://localhost:8084"