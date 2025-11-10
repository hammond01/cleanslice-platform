# Quick Start Script for CleanSlice Platform

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  CleanSlice Platform - Quick Start  " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Package
Write-Host "[1/3] Packaging services..." -ForegroundColor Yellow
& .\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Build successful!" -ForegroundColor Green
Write-Host ""

# Step 2: Start infrastructure
Write-Host "[2/3] Starting infrastructure (Postgres, Kafka, MinIO, Keycloak)..." -ForegroundColor Yellow
Set-Location deploy
docker-compose up -d postgres-files postgres-audit postgres-product kafka zookeeper minio keycloak postgres-keycloak prometheus grafana
Write-Host "✓ Infrastructure started!" -ForegroundColor Green
Write-Host ""

# Wait for services to be ready
Write-Host "Waiting for services to be ready (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Step 3: Start application services
Write-Host "[3/3] Starting application services..." -ForegroundColor Yellow
docker-compose up -d gateway-service files-service audit-service product-service
Set-Location ..

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "  ✓ All services started!            " -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
Write-Host "Access URLs:" -ForegroundColor Cyan
Write-Host "  Gateway:    http://localhost:8081" -ForegroundColor White
Write-Host "  Files API:  http://localhost:8081/api/files" -ForegroundColor White
Write-Host "  Audit API:  http://localhost:8081/api/audit" -ForegroundColor White
Write-Host "  Product API: http://localhost:8081/api/products" -ForegroundColor White
Write-Host "  Swagger UI: http://localhost:8082/swagger-ui.html (Files)" -ForegroundColor White
Write-Host "  MinIO:      http://localhost:9001 (minioadmin/minioadmin)" -ForegroundColor White
Write-Host "  Keycloak:   http://localhost:8080 (admin/admin)" -ForegroundColor White
Write-Host "  Grafana:    http://localhost:3000 (admin/admin)" -ForegroundColor White
Write-Host ""
Write-Host "To view logs: docker-compose -f deploy/docker-compose.yml logs -f" -ForegroundColor Yellow
Write-Host "To stop: docker-compose -f deploy/docker-compose.yml down" -ForegroundColor Yellow
