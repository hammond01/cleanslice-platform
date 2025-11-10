# Run Product Service locally with H2 in-memory database
# No Docker dependencies required

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Product Service (Local Mode)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Profile: dev-local (H2 in-memory database)" -ForegroundColor Yellow
Write-Host "Port: 8084" -ForegroundColor Yellow
Write-Host "Swagger UI: http://localhost:8084/swagger-ui.html" -ForegroundColor Green
Write-Host "H2 Console: http://localhost:8084/h2-console" -ForegroundColor Green
Write-Host ""

# Set environment
$env:SPRING_PROFILES_ACTIVE = "dev-local"

# Run with Maven from root directory
Write-Host "Starting application..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run -pl services/product-service/product-adapters-in-rest "-Dspring-boot.run.profiles=dev-local"
