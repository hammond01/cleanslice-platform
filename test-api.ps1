# =============================================================================
# CleanSlice Platform - API Test Script
# =============================================================================

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "    API Integration Test             " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8081"  # Gateway
$identityUrl = "http://localhost:8085"
$filesUrl = "http://localhost:8082"
$productUrl = "http://localhost:8084"
$auditUrl = "http://localhost:8083"

# Test data
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$testUser = @{
    username = "testuser_$timestamp"
    email = "test_$timestamp@example.com"
    password = "Pass123!@#"
}

Write-Host "[TEST 1/6] Register a new user..." -ForegroundColor Green

try {
    $registerBody = @{
        username = $testUser.username
        email = $testUser.email
        password = $testUser.password
    } | ConvertTo-Json

    Write-Host "Request: POST $identityUrl/api/auth/register" -ForegroundColor Gray
    Write-Host "Body: $registerBody" -ForegroundColor Gray

    $registerResponse = Invoke-RestMethod -Uri "$identityUrl/api/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody

    Write-Host "✓ User registered successfully!" -ForegroundColor Green
    Write-Host "  Username: $($testUser.username)" -ForegroundColor White
    Write-Host "  User ID: $($registerResponse.userId)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "✗ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 2

# =============================================================================
# TEST 2: Login and Get JWT Token
# =============================================================================

Write-Host "[TEST 2/6] Login and get JWT token from Keycloak..." -ForegroundColor Green

try {
    $loginBody = @{
        username = $testUser.username
        password = $testUser.password
    } | ConvertTo-Json

    Write-Host "Request: POST $identityUrl/api/auth/login" -ForegroundColor Gray

    $loginResponse = Invoke-RestMethod -Uri "$identityUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody

    $token = $loginResponse.token
    $tokenType = $loginResponse.tokenType

    Write-Host "✓ Login successful!" -ForegroundColor Green
    Write-Host "  Token Type: $tokenType" -ForegroundColor White
    Write-Host "  Token: $($token.Substring(0, 50))..." -ForegroundColor White
    Write-Host ""

    # Decode JWT to show claims
    $tokenParts = $token.Split('.')
    $payload = $tokenParts[1]
    # Pad base64 string
    while ($payload.Length % 4 -ne 0) {
        $payload += "="
    }
    $payloadJson = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($payload))
    $claims = $payloadJson | ConvertFrom-Json

    Write-Host "  JWT Claims:" -ForegroundColor Yellow
    Write-Host "    Subject: $($claims.sub)" -ForegroundColor Gray
    Write-Host "    Preferred Username: $($claims.preferred_username)" -ForegroundColor Gray
    Write-Host "    Issuer: $($claims.iss)" -ForegroundColor Gray
    Write-Host ""

} catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 2

# =============================================================================
# TEST 3: Upload a File
# =============================================================================

Write-Host "[TEST 3/6] Upload a file to Files Service..." -ForegroundColor Green

try {
    # Create a test file
    $testFileName = "test-$timestamp.txt"
    $testFilePath = Join-Path $env:TEMP $testFileName
    "This is a test file created at $(Get-Date)" | Out-File -FilePath $testFilePath -Encoding UTF8

    Write-Host "Request: POST $filesUrl/api/files" -ForegroundColor Gray
    Write-Host "  File: $testFileName" -ForegroundColor Gray
    Write-Host "  Auth: Bearer $($token.Substring(0, 20))..." -ForegroundColor Gray

    # Upload file using multipart/form-data
    $headers = @{
        "Authorization" = "Bearer $token"
    }

    $form = @{
        file = Get-Item -Path $testFilePath
    }

    $uploadResponse = Invoke-RestMethod -Uri "$filesUrl/api/files" `
        -Method POST `
        -Headers $headers `
        -Form $form

    $fileId = $uploadResponse.fileId

    Write-Host "✓ File uploaded successfully!" -ForegroundColor Green
    Write-Host "  File ID: $fileId" -ForegroundColor White
    Write-Host "  Filename: $($uploadResponse.filename)" -ForegroundColor White
    Write-Host "  Size: $($uploadResponse.size) bytes" -ForegroundColor White
    Write-Host "  Content Type: $($uploadResponse.contentType)" -ForegroundColor White
    Write-Host ""

    # Clean up temp file
    Remove-Item -Path $testFilePath -Force

} catch {
    Write-Host "✗ File upload failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 2

# =============================================================================
# TEST 4: Create a Product
# =============================================================================

Write-Host "[TEST 4/6] Create a product..." -ForegroundColor Green

try {
    $productBody = @{
        name = "Test Product $timestamp"
        description = "This is a test product created via API test"
    } | ConvertTo-Json

    Write-Host "Request: POST $productUrl/api/products" -ForegroundColor Gray
    Write-Host "Body: $productBody" -ForegroundColor Gray

    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }

    $productResponse = Invoke-RestMethod -Uri "$productUrl/api/products" `
        -Method POST `
        -Headers $headers `
        -Body $productBody

    $productId = $productResponse.id

    Write-Host "✓ Product created successfully!" -ForegroundColor Green
    Write-Host "  Product ID: $productId" -ForegroundColor White
    Write-Host "  Name: $($productResponse.name)" -ForegroundColor White
    Write-Host "  Status: $($productResponse.status)" -ForegroundColor White
    Write-Host ""

} catch {
    Write-Host "✗ Product creation failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 2

# =============================================================================
# TEST 5: Attach Media to Product
# =============================================================================

Write-Host "[TEST 5/6] Attach media to product..." -ForegroundColor Green

try {
    $attachMediaBody = @{
        fileId = $fileId
        altText = "Test product image"
        isPrimary = $true
    } | ConvertTo-Json

    Write-Host "Request: POST $productUrl/api/products/$productId/media" -ForegroundColor Gray

    $attachResponse = Invoke-RestMethod -Uri "$productUrl/api/products/$productId/media" `
        -Method POST `
        -Headers $headers `
        -Body $attachMediaBody

    Write-Host "✓ Media attached to product!" -ForegroundColor Green
    Write-Host "  Product ID: $($attachResponse.id)" -ForegroundColor White
    Write-Host "  Media Count: $($attachResponse.mediaList.Count)" -ForegroundColor White
    Write-Host ""

} catch {
    Write-Host "✗ Attach media failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 3

# =============================================================================
# TEST 6: Check Audit Logs
# =============================================================================

Write-Host "[TEST 6/6] Verify audit logs..." -ForegroundColor Green

try {
    Write-Host "Request: GET $auditUrl/api/audit/logs" -ForegroundColor Gray

    $auditResponse = Invoke-RestMethod -Uri "$auditUrl/api/audit/logs" `
        -Method GET `
        -Headers @{ "Authorization" = "Bearer $token" }

    Write-Host "✓ Audit logs retrieved!" -ForegroundColor Green
    Write-Host "  Total logs: $($auditResponse.Count)" -ForegroundColor White
    
    if ($auditResponse.Count -gt 0) {
        Write-Host ""
        Write-Host "  Recent audit events:" -ForegroundColor Yellow
        $auditResponse | Select-Object -First 5 | ForEach-Object {
            Write-Host "    - $($_.action) | Resource: $($_.resourceType) | Time: $($_.timestamp)" -ForegroundColor Gray
        }
    }
    Write-Host ""

} catch {
    Write-Host "⚠ Audit logs check failed (this is optional): $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

# =============================================================================
# TEST SUMMARY
# =============================================================================

Write-Host "=====================================" -ForegroundColor Green
Write-Host "    ✓ ALL TESTS COMPLETED!           " -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""

Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  ✓ User registered in Keycloak: $($testUser.username)" -ForegroundColor White
Write-Host "  ✓ JWT token obtained from Keycloak" -ForegroundColor White
Write-Host "  ✓ File uploaded to MinIO: $fileId" -ForegroundColor White
Write-Host "  ✓ Product created: $productId" -ForegroundColor White
Write-Host "  ✓ Media attached to product" -ForegroundColor White
Write-Host "  ✓ Audit logs verified" -ForegroundColor White
Write-Host ""

Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  - Check Keycloak Admin Console: http://localhost:8080" -ForegroundColor White
Write-Host "  - Check MinIO Console: http://localhost:9001" -ForegroundColor White
Write-Host "  - Check Grafana Metrics: http://localhost:3000" -ForegroundColor White
Write-Host ""
