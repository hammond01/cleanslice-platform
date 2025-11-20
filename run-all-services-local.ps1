# Run both Product and Audit services locally with PostgreSQL
# Requires Docker Compose infrastructure running (postgres, kafka)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting All Services (Local Mode)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if infrastructure is running
Write-Host "Checking infrastructure..." -ForegroundColor Cyan

$pgRunning = docker ps --filter "name=deploy-postgres-1" --filter "status=running" --format "{{.Names}}"
$kafkaRunning = docker ps --filter "name=deploy-kafka-1" --filter "status=running" --format "{{.Names}}"

if (-not $pgRunning) {
    Write-Host "ERROR: PostgreSQL is not running!" -ForegroundColor Red
    Write-Host "Starting infrastructure..." -ForegroundColor Yellow
    cd deploy
    docker-compose up -d postgres kafka zookeeper
    cd ..
    Start-Sleep -Seconds 5
}

if (-not $kafkaRunning) {
    Write-Host "Starting Kafka..." -ForegroundColor Yellow
    cd deploy
    docker-compose up -d kafka zookeeper
    cd ..
    Start-Sleep -Seconds 5
}

Write-Host "Infrastructure OK!" -ForegroundColor Green
Write-Host ""
Write-Host "Services will be available at:" -ForegroundColor Yellow
Write-Host "  Gateway Service: http://localhost:8081 (API Gateway)" -ForegroundColor Magenta
Write-Host "  Product Service: http://localhost:8084" -ForegroundColor Green
Write-Host "  Product Swagger: http://localhost:8084/swagger-ui.html" -ForegroundColor Green
Write-Host "  Audit Service:   http://localhost:8083" -ForegroundColor Green
Write-Host "  Audit Swagger:   http://localhost:8083/swagger-ui.html" -ForegroundColor Green
Write-Host ""
Write-Host "Access via Gateway:" -ForegroundColor Yellow
Write-Host "  Products: http://localhost:8081/api/products/**" -ForegroundColor Cyan
Write-Host "  Audit:    http://localhost:8081/api/audit/**" -ForegroundColor Cyan
Write-Host "  Files:    http://localhost:8081/api/files/**" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C in each terminal to stop services" -ForegroundColor Yellow
Write-Host ""

# Start Gateway Service in new window
Write-Host "Starting Gateway Service..." -ForegroundColor Cyan
Start-Process pwsh -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\run-gateway-local.ps1"

# Wait a bit before starting next service
Start-Sleep -Seconds 2

# Start Product Service in new window
Write-Host "Starting Product Service..." -ForegroundColor Cyan
Start-Process pwsh -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\run-product-local-postgres.ps1"

# Wait a bit before starting next service
Start-Sleep -Seconds 2

# Start Files Service in new window
Write-Host "Starting Files Service..." -ForegroundColor Cyan
Start-Process pwsh -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\run-files-local-postgres.ps1"

# Wait a bit before starting next service
Start-Sleep -Seconds 2

# Start Audit Service in new window
Write-Host "Starting Audit Service..." -ForegroundColor Cyan
Start-Process pwsh -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\run-audit-local.ps1"

Write-Host ""
Write-Host "All services starting in separate windows!" -ForegroundColor Green
Write-Host "Gateway will be ready first, then backend services." -ForegroundColor Yellow
Write-Host "Check the new terminal windows for service logs." -ForegroundColor Yellow
