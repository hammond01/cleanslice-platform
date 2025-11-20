# Run Gateway Service locally
# Routes requests to other services

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Gateway Service (Local Mode)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Port: 8081" -ForegroundColor Yellow
Write-Host "Gateway URL: http://localhost:8081" -ForegroundColor Green
Write-Host ""
Write-Host "Routes:" -ForegroundColor Yellow
Write-Host "  /api/products/** -> Product Service (8084)" -ForegroundColor Green
Write-Host "  /api/audit/**    -> Audit Service (8083)" -ForegroundColor Green
Write-Host "  /api/files/**    -> Files Service (8082)" -ForegroundColor Green
Write-Host ""

# Run with Maven from root directory
Write-Host "Starting application..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run -pl services/gateway-service
