# Stop all services

Write-Host "Stopping all services..." -ForegroundColor Yellow
Set-Location deploy
docker-compose down
Set-Location ..
Write-Host "✓ All services stopped!" -ForegroundColor Green
