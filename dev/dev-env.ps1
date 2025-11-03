# Development Environment Management Script for Windows

param(
    [Parameter(Position=0)]
    [ValidateSet("start", "stop", "restart", "clean", "status", "logs", "minio", "test-s3", "full", "help")]
    [string]$Command = "help",
    
    [Parameter()]
    [switch]$Full
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Determine which compose file to use
if ($Full -or $Command -eq "full") {
    $ComposeFile = Join-Path $ScriptDir "docker-compose-full.yml"
    $Mode = "FULL"
} else {
    $ComposeFile = Join-Path $ScriptDir "docker-compose.yml"
    $Mode = "DEV"
}

function Show-Help {
    Write-Host "Usage: .\dev-env.ps1 [COMMAND] [-Full]" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor Yellow
    Write-Host "  start     Start the development environment (infrastructure only)"
    Write-Host "  stop      Stop the development environment"
    Write-Host "  restart   Restart the development environment"
    Write-Host "  clean     Stop and remove all containers and volumes"
    Write-Host "  status    Show status of all services"
    Write-Host "  logs      Show logs for all services"
    Write-Host "  minio     Open MinIO console in browser"
    Write-Host "  test-s3   Test S3 functionality"
    Write-Host "  full      Start FULL environment (infrastructure + all services)"
    Write-Host "  help      Show this help message"
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  -Full     Use full compose file (all services in containers)"
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Green
    Write-Host "  .\dev-env.ps1 start          # Start infrastructure only (default)"
    Write-Host "  .\dev-env.ps1 start -Full    # Start all services in containers"
    Write-Host "  .\dev-env.ps1 full           # Start all services in containers"
    Write-Host ""
    Write-Host "Development Modes:" -ForegroundColor Cyan
    Write-Host "  DEV Mode:  Infrastructure only - run Spring services natively"
    Write-Host "  FULL Mode: All services in containers"
}

function Start-Services {
    Write-Host "🚀 Starting development environment ($Mode mode)..." -ForegroundColor Green
    docker-compose -f $ComposeFile up -d
    Write-Host "✅ Development environment started!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📍 Infrastructure URLs:" -ForegroundColor Cyan
    Write-Host "   MinIO Console: http://localhost:9001 (minioadmin/minioadmin)"
    Write-Host "   MinIO S3 API:  http://localhost:9000"
    Write-Host "   PostgreSQL:    localhost:5432 (postgres/password)"
    Write-Host "   Redis:         localhost:6379"
    Write-Host "   Kafka:         localhost:9092"
    Write-Host "   Kafka UI:      http://localhost:8085"
    
    if ($Mode -eq "FULL") {
        Write-Host ""
        Write-Host "📍 Application URLs:" -ForegroundColor Cyan
        Write-Host "   API Gateway:   http://localhost:8080"
        Write-Host "   Eureka:        http://localhost:8761"
        Write-Host "   Security:      http://localhost:8081"
        Write-Host "   Catalog:       http://localhost:8082"
        Write-Host "   Order:         http://localhost:8083"
        Write-Host "   Cart:          http://localhost:8084"
    } else {
        Write-Host ""
        Write-Host "💡 To run Spring services natively:" -ForegroundColor Yellow
        Write-Host "   cd backend/services/eureka-server && mvn spring-boot:run"
        Write-Host "   cd backend/services/security-service && mvn spring-boot:run"
        Write-Host "   cd backend/services/gateway && mvn spring-boot:run"
        Write-Host "   cd backend/services/catalog-service && mvn spring-boot:run"
        Write-Host "   cd backend/services/cart-service && mvn spring-boot:run"
        Write-Host "   cd backend/services/order-service && mvn spring-boot:run"
    }
}

function Stop-Services {
    Write-Host "🛑 Stopping development environment ($Mode mode)..." -ForegroundColor Yellow
    docker-compose -f $ComposeFile down
    Write-Host "✅ Development environment stopped!" -ForegroundColor Green
}

function Restart-Services {
    Stop-Services
    Start-Services
}

function Clean-All {
    Write-Host "🧹 Cleaning up development environment ($Mode mode)..." -ForegroundColor Yellow
    docker-compose -f $ComposeFile down -v --remove-orphans
    Write-Host "✅ Development environment cleaned!" -ForegroundColor Green
}

function Show-Status {
    Write-Host "📊 Development environment status ($Mode mode):" -ForegroundColor Cyan
    docker-compose -f $ComposeFile ps
}

function Show-Logs {
    docker-compose -f $ComposeFile logs -f
}

function Open-Minio {
    Write-Host "🌐 Opening MinIO console..." -ForegroundColor Cyan
    Start-Process "http://localhost:9001"
}

function Test-S3 {
    Write-Host "🧪 Testing S3 functionality..." -ForegroundColor Cyan
    Write-Host "Make sure the catalog service is running on port 8082" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Testing single file upload URL generation..." -ForegroundColor Cyan
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8082/api/images/upload-url?fileName=test.jpg&contentType=image/jpeg" -Method POST
        $response | ConvertTo-Json -Depth 3
        Write-Host ""
        Write-Host "2. Testing bulk upload URL generation..." -ForegroundColor Cyan
        
        $bulkRequest = @{
            files = @(
                @{
                    fileName = "test1.jpg"
                    contentType = "image/jpeg"
                    fileSize = 1024000
                },
                @{
                    fileName = "test2.png"
                    contentType = "image/png"
                    fileSize = 2048000
                }
            )
        } | ConvertTo-Json -Depth 3
        
        $bulkResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/images/bulk-upload-urls" -Method POST -Body $bulkRequest -ContentType "application/json"
        $bulkResponse | ConvertTo-Json -Depth 3
        Write-Host ""
        Write-Host "3. Check MinIO console at http://localhost:9001 to see if bucket exists" -ForegroundColor Yellow
    }
    catch {
        Write-Host "❌ Error testing S3: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "Make sure the catalog service is running!" -ForegroundColor Yellow
    }
}

switch ($Command) {
    "start" { Start-Services }
    "stop" { Stop-Services }
    "restart" { Restart-Services }
    "clean" { Clean-All }
    "status" { Show-Status }
    "logs" { Show-Logs }
    "minio" { Open-Minio }
    "test-s3" { Test-S3 }
    "full" { 
        $ComposeFile = Join-Path $ScriptDir "docker-compose-full.yml"
        $Mode = "FULL"
        Start-Services 
    }
    "help" { Show-Help }
    default {
        Write-Host "❌ Unknown command: $Command" -ForegroundColor Red
        Write-Host ""
        Show-Help
        exit 1
    }
}