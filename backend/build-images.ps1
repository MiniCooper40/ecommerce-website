#!/usr/bin/env pwsh
# Build Docker images for all microservices using Jib

$ErrorActionPreference = "Stop"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Building Docker Images with Jib" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$services = @(
    "services/eureka-server",
    "services/gateway",
    "services/security-service",
    "services/catalog-service",
    "services/cart-service",
    "services/order-service"
)

# Get the directory where this script is located (should be backend/)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

if (-not (Test-Path "pom.xml")) {
    Write-Host "Error: Could not find pom.xml in current directory" -ForegroundColor Red
    Write-Host "Current directory: $(Get-Location)" -ForegroundColor Red
    exit 1
}

$successCount = 0
$failCount = 0
$failed = @()

foreach ($service in $services) {
    $serviceName = Split-Path -Leaf $service
    Write-Host "[$($successCount + $failCount + 1)/$($services.Count)] Building $serviceName..." -ForegroundColor Yellow
    
    Push-Location $service
    $result = mvn jib:dockerBuild -DskipTests -q 2>&1
    $exitCode = $LASTEXITCODE
    Pop-Location
    
    if ($exitCode -eq 0) {
        Write-Host "  ✓ $serviceName - SUCCESS" -ForegroundColor Green
        $successCount++
    } else {
        Write-Host "  ✗ $serviceName - FAILED" -ForegroundColor Red
        $failCount++
        $failed += $serviceName
    }
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Build Summary" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Success: $successCount" -ForegroundColor Green
Write-Host "Failed:  $failCount" -ForegroundColor Red

if ($failCount -gt 0) {
    Write-Host ""
    Write-Host "Failed services:" -ForegroundColor Red
    foreach ($f in $failed) {
        Write-Host "  - $f" -ForegroundColor Red
    }
    exit 1
}

Write-Host ""
Write-Host "All Docker images built successfully! 🎉" -ForegroundColor Green
Write-Host ""
Write-Host "Verify with: docker images | grep ecommerce" -ForegroundColor Cyan
