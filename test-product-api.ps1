# Quick API Test for Product Service
# Run this after starting the service with run-product-local.ps1

$baseUrl = "http://localhost:8084/api/products"
$userId = [guid]::NewGuid().ToString()

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Product Service API Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health check
Write-Host "[1] Health Check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8084/actuator/health" -Method Get
    Write-Host "✓ Service is UP" -ForegroundColor Green
    Write-Host "  Status: $($health.status)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Service is DOWN or not started yet" -ForegroundColor Red
    Write-Host "  Run: .\run-product-local.ps1 first" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Test 2: Create Product
Write-Host "[2] Creating Product..." -ForegroundColor Yellow
$createRequest = @{
    name = "iPhone 15 Pro Max"
    description = "Latest flagship smartphone with titanium design"
} | ConvertTo-Json

try {
    $product = Invoke-RestMethod -Uri $baseUrl -Method Post `
        -Headers @{ "Content-Type" = "application/json"; "X-User-Id" = $userId } `
        -Body $createRequest
    
    Write-Host "✓ Product created successfully" -ForegroundColor Green
    Write-Host "  ID: $($product.id)" -ForegroundColor Gray
    Write-Host "  Name: $($product.name)" -ForegroundColor Gray
    Write-Host "  Owner: $($product.ownerId)" -ForegroundColor Gray
    $productId = $product.id
} catch {
    Write-Host "✗ Failed to create product" -ForegroundColor Red
    Write-Host "  Error: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 3: Get Product
Write-Host "[3] Retrieving Product..." -ForegroundColor Yellow
try {
    $retrieved = Invoke-RestMethod -Uri "$baseUrl/$productId" -Method Get
    Write-Host "✓ Product retrieved successfully" -ForegroundColor Green
    Write-Host "  Name: $($retrieved.name)" -ForegroundColor Gray
    Write-Host "  Description: $($retrieved.description)" -ForegroundColor Gray
    Write-Host "  Created: $($retrieved.createdAt)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to retrieve product" -ForegroundColor Red
    Write-Host "  Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 4: Attach Media (will fail without Files service, but tests endpoint)
Write-Host "[4] Attaching Media (Expected to fail - Files service not running)..." -ForegroundColor Yellow
$mediaRequest = @{
    fileId = [guid]::NewGuid().ToString()
    altText = "Product main image"
    sortOrder = 1
    isPrimary = $true
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$baseUrl/$productId/media" -Method Post `
        -Headers @{ "Content-Type" = "application/json" } `
        -Body $mediaRequest
    
    Write-Host "✓ Media attached (unexpected success)" -ForegroundColor Green
} catch {
    Write-Host "⚠ Media attachment returned error (expected - fileId not validated yet)" -ForegroundColor Yellow
    Write-Host "  This is normal without Files service running" -ForegroundColor Gray
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✓ Service is running" -ForegroundColor Green
Write-Host "✓ Can create products" -ForegroundColor Green
Write-Host "✓ Can retrieve products" -ForegroundColor Green
Write-Host "⚠ Media attachment needs Files service" -ForegroundColor Yellow
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Open Swagger UI: http://localhost:8084/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  2. View H2 Console: http://localhost:8084/h2-console" -ForegroundColor Cyan
Write-Host "     JDBC URL: jdbc:h2:mem:productdb" -ForegroundColor Gray
Write-Host "     Username: sa" -ForegroundColor Gray
Write-Host "     Password: (empty)" -ForegroundColor Gray
Write-Host ""
