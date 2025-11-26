# Run Identity Service locally
# Requires infrastructure running (postgres, kafka, keycloak if using Keycloak auth)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Identity Service (Local Mode)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if required infrastructure is running
Write-Host "Checking infrastructure..." -ForegroundColor Cyan

$pgRunning = docker ps --filter "name=deploy-postgres-1" --filter "status=running" --format "{{.Names}}"

if (-not $pgRunning) {
    Write-Host "ERROR: PostgreSQL is not running!" -ForegroundColor Red
    Write-Host "Please start infrastructure first with: cd deploy; docker-compose up -d postgres" -ForegroundColor Yellow
    exit 1
}

Write-Host "Infrastructure OK!" -ForegroundColor Green
Write-Host ""

# Set environment variables for local development
$env:SPRING_PROFILES_ACTIVE = "local"
$env:JAVA_OPTS = "-Xmx512m -Xms256m"

Write-Host "Starting Identity Service..." -ForegroundColor Cyan
Write-Host "Service will be available at: http://localhost:8085" -ForegroundColor Green
Write-Host "Swagger UI: http://localhost:8085/swagger-ui.html" -ForegroundColor Green
Write-Host "Database: PostgreSQL (localhost:5432/identitydb)" -ForegroundColor Green
Write-Host ""
Write-Host "To switch to Keycloak authentication:" -ForegroundColor Yellow
Write-Host "  1. Start Keycloak: cd deploy; docker-compose up -d keycloak postgres-keycloak" -ForegroundColor Yellow
Write-Host "  2. Set auth.provider=keycloak in application.yml" -ForegroundColor Yellow
Write-Host "  3. Restart the service" -ForegroundColor Yellow
Write-Host ""
Write-Host "Press Ctrl+C to stop the service" -ForegroundColor Yellow
Write-Host ""

# Change to identity service directory and run
cd services/identity
mvn spring-boot:run