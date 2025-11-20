# Run Files Service locally with PostgreSQL
# Requires Docker Compose infrastructure running (postgres, kafka)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Files Service (Local Mode)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Prerequisites:" -ForegroundColor Yellow
Write-Host "  - PostgreSQL running on localhost:5432" -ForegroundColor Yellow
Write-Host "  - Kafka running on localhost:9092" -ForegroundColor Yellow
Write-Host "  - Database 'filesdb' must exist" -ForegroundColor Yellow
Write-Host ""
Write-Host "Port: 8082" -ForegroundColor Yellow
Write-Host "Swagger UI: http://localhost:8082/swagger-ui.html" -ForegroundColor Green
Write-Host ""

# Check if infrastructure is running
Write-Host "Checking infrastructure..." -ForegroundColor Cyan

# Check PostgreSQL
$pgRunning = docker ps --filter "name=deploy-postgres-1" --filter "status=running" --format "{{.Names}}"
if (-not $pgRunning) {
    Write-Host "ERROR: PostgreSQL container is not running!" -ForegroundColor Red
    Write-Host "Please run: cd deploy && docker-compose up -d postgres" -ForegroundColor Yellow
    exit 1
}

# Check Kafka
$kafkaRunning = docker ps --filter "name=deploy-kafka-1" --filter "status=running" --format "{{.Names}}"
if (-not $kafkaRunning) {
    Write-Host "WARNING: Kafka container is not running!" -ForegroundColor Yellow
    Write-Host "Kafka features may not work. To start: cd deploy && docker-compose up -d kafka zookeeper" -ForegroundColor Yellow
}

Write-Host "Infrastructure OK!" -ForegroundColor Green
Write-Host ""

# Run with Maven from root directory
Write-Host "Starting application..." -ForegroundColor Cyan
.\mvnw.cmd spring-boot:run -pl services/files-service
