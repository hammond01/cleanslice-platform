# Quick Start - Simplified Version
# Just infrastructure + manual service start

Write-Host "Starting Infrastructure Only..." -ForegroundColor Cyan

cd deploy
docker-compose up -d postgres kafka zookeeper minio keycloak postgres-keycloak prometheus grafana
cd ..

Write-Host ""
Write-Host "Waiting for infrastructure (30s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host ""
Write-Host "✓ Infrastructure ready!" -ForegroundColor Green
Write-Host ""
Write-Host "Build services:" -ForegroundColor Yellow
Write-Host "  ./mvnw.cmd clean package -DskipTests" -ForegroundColor White
Write-Host ""
Write-Host "Start services in separate terminals:" -ForegroundColor Yellow
Write-Host "  java -jar services/gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host "  java -jar services/identity/target/identity-1.0.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host "  java -jar services/files-service/target/files-service-1.0.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host "  java -jar services/product-service/target/product-service-1.0.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host "  java -jar services/audit-service/audit-adapters-in-rest/target/audit-service-1.0.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host ""
