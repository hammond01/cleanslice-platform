# =============================================================================
# CleanSlice Platform - Test Guide (Infrastructure Docker + Services Windows)
# =============================================================================

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host " CleanSlice Platform - Test Guide   " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Prerequisites:" -ForegroundColor Yellow
Write-Host "  - Docker Desktop running" -ForegroundColor White
Write-Host "  - Java 21 installed" -ForegroundColor White
Write-Host "  - Ports available: 8080-8085, 5432, 9000-9001, 9090-9092, 2181, 3000" -ForegroundColor White
Write-Host ""

# =============================================================================
# STEP 1: Start Infrastructure (Docker)
# =============================================================================

Write-Host "[STEP 1/5] Starting Infrastructure with Docker..." -ForegroundColor Green
Write-Host "  - PostgreSQL (port 5432)" -ForegroundColor Gray
Write-Host "  - Kafka + Zookeeper (ports 9092, 2181)" -ForegroundColor Gray
Write-Host "  - MinIO (ports 9000, 9001)" -ForegroundColor Gray
Write-Host "  - Keycloak (port 8080)" -ForegroundColor Gray
Write-Host "  - Prometheus (port 9090)" -ForegroundColor Gray
Write-Host "  - Grafana (port 3000)" -ForegroundColor Gray
Write-Host ""

Set-Location deploy
docker-compose up -d postgres kafka zookeeper minio keycloak postgres-keycloak prometheus grafana

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start infrastructure!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Waiting for infrastructure to be ready (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check if services are running
Write-Host "Checking infrastructure health..." -ForegroundColor Yellow
docker-compose ps

Set-Location ..
Write-Host ""
Write-Host "✓ Infrastructure is ready!" -ForegroundColor Green
Write-Host ""

# =============================================================================
# STEP 2: Build All Services
# =============================================================================

Write-Host "[STEP 2/5] Building all services..." -ForegroundColor Green
./mvnw.cmd clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Build successful!" -ForegroundColor Green
Write-Host ""

# =============================================================================
# STEP 3: Start Services on Windows
# =============================================================================

Write-Host "[STEP 3/5] Starting services on Windows..." -ForegroundColor Green
Write-Host ""

# Create a function to start service in new window
function Start-ServiceWindow {
    param(
        [string]$ServiceName,
        [string]$JarPath,
        [string]$Port
    )
    
    $title = "$ServiceName (Port: $Port)"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", `
        "Write-Host 'Starting $ServiceName...' -ForegroundColor Cyan; " +
        "java -jar '$JarPath'; " +
        "Write-Host '$ServiceName stopped' -ForegroundColor Red"
}

Write-Host "Starting Gateway Service (Port 8081)..." -ForegroundColor Cyan
Start-ServiceWindow -ServiceName "Gateway Service" `
    -JarPath "services\gateway-service\target\gateway-service-1.0.0-SNAPSHOT.jar" `
    -Port "8081"
Start-Sleep -Seconds 5

Write-Host "Starting Identity Service (Port 8085)..." -ForegroundColor Cyan
Start-ServiceWindow -ServiceName "Identity Service" `
    -JarPath "services\identity\target\identity-1.0.0-SNAPSHOT.jar" `
    -Port "8085"
Start-Sleep -Seconds 5

Write-Host "Starting Files Service (Port 8082)..." -ForegroundColor Cyan
Start-ServiceWindow -ServiceName "Files Service" `
    -JarPath "services\files-service\target\files-service-1.0.0-SNAPSHOT.jar" `
    -Port "8082"
Start-Sleep -Seconds 5

Write-Host "Starting Product Service (Port 8084)..." -ForegroundColor Cyan
Start-ServiceWindow -ServiceName "Product Service" `
    -JarPath "services\product-service\target\product-service-1.0.0-SNAPSHOT.jar" `
    -Port "8084"
Start-Sleep -Seconds 5

Write-Host "Starting Audit Service (Port 8083)..." -ForegroundColor Cyan
Start-ServiceWindow -ServiceName "Audit Service" `
    -JarPath "services\audit-service\audit-adapters-in-rest\target\audit-service-1.0.0-SNAPSHOT.jar" `
    -Port "8083"

Write-Host ""
Write-Host "Waiting for all services to start (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "✓ All services started in separate windows!" -ForegroundColor Green
Write-Host ""

# =============================================================================
# STEP 4: Health Check
# =============================================================================

Write-Host "[STEP 4/5] Checking service health..." -ForegroundColor Green

function Test-ServiceHealth {
    param(
        [string]$Name,
        [string]$Url
    )
    
    try {
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "  ✓ $Name is healthy" -ForegroundColor Green
            return $true
        }
    } catch {
        Write-Host "  ✗ $Name is not responding" -ForegroundColor Red
        return $false
    }
}

Test-ServiceHealth -Name "Gateway Service" -Url "http://localhost:8081/actuator/health"
Test-ServiceHealth -Name "Identity Service" -Url "http://localhost:8085/actuator/health"
Test-ServiceHealth -Name "Files Service" -Url "http://localhost:8082/actuator/health"
Test-ServiceHealth -Name "Product Service" -Url "http://localhost:8084/actuator/health"
Test-ServiceHealth -Name "Audit Service" -Url "http://localhost:8083/actuator/health"
Test-ServiceHealth -Name "Keycloak" -Url "http://localhost:8080/health"
Test-ServiceHealth -Name "MinIO" -Url "http://localhost:9000/minio/health/live"

Write-Host ""

# =============================================================================
# STEP 5: Display Access Information
# =============================================================================

Write-Host "[STEP 5/5] System is ready for testing!" -ForegroundColor Green
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "       ACCESS INFORMATION            " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "API Services:" -ForegroundColor Yellow
Write-Host "  Gateway:         http://localhost:8081" -ForegroundColor White
Write-Host "  Identity:        http://localhost:8085" -ForegroundColor White
Write-Host "  Files:           http://localhost:8082" -ForegroundColor White
Write-Host "  Product:         http://localhost:8084" -ForegroundColor White
Write-Host "  Audit:           http://localhost:8083" -ForegroundColor White
Write-Host ""

Write-Host "Swagger UI:" -ForegroundColor Yellow
Write-Host "  Identity:        http://localhost:8085/swagger-ui.html" -ForegroundColor White
Write-Host "  Files:           http://localhost:8082/swagger-ui.html" -ForegroundColor White
Write-Host "  Product:         http://localhost:8084/swagger-ui.html" -ForegroundColor White
Write-Host "  Audit:           http://localhost:8083/swagger-ui.html" -ForegroundColor White
Write-Host ""

Write-Host "Infrastructure:" -ForegroundColor Yellow
Write-Host "  Keycloak Admin:  http://localhost:8080 (admin/admin)" -ForegroundColor White
Write-Host "  MinIO Console:   http://localhost:9001 (minioadmin/minioadmin)" -ForegroundColor White
Write-Host "  Prometheus:      http://localhost:9090" -ForegroundColor White
Write-Host "  Grafana:         http://localhost:3000 (admin/admin)" -ForegroundColor White
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "       TEST SCENARIOS                " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Now run: " -ForegroundColor Yellow
Write-Host "  .\test-api.ps1" -ForegroundColor Green
Write-Host ""
Write-Host "This will execute:" -ForegroundColor Gray
Write-Host "  1. Register a new user in Keycloak" -ForegroundColor Gray
Write-Host "  2. Login and get JWT token" -ForegroundColor Gray
Write-Host "  3. Upload a file to MinIO" -ForegroundColor Gray
Write-Host "  4. Create a product" -ForegroundColor Gray
Write-Host "  5. Attach media to product" -ForegroundColor Gray
Write-Host "  6. Verify audit logs" -ForegroundColor Gray
Write-Host ""

Write-Host "To stop services:" -ForegroundColor Yellow
Write-Host "  1. Close all service PowerShell windows" -ForegroundColor White
Write-Host "  2. Run: cd deploy; docker-compose down" -ForegroundColor White
Write-Host ""
