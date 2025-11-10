# Quick Test Guide

## Prerequisites
Ensure all services are running: `.\start.ps1`

## 1. Test Files Service

### Upload a file
```powershell
# Generate a test file
"Test content" | Out-File -FilePath test.txt

# Upload
$userId = [guid]::NewGuid()
$response = curl -X POST http://localhost:8082/api/files `
  -H "X-User-Id: $userId" `
  -F "file=@test.txt" `
  -UseBasicParsing

$response.Content | ConvertFrom-Json
# Save the fileId from response
```

### Get download URL (presigned)
```powershell
$fileId = "YOUR_FILE_ID_HERE"
curl -i http://localhost:8082/api/files/$fileId/download
# Should return HTTP 302 with Location header pointing to MinIO presigned URL
```

### Verify file in MinIO
Open http://localhost:9001
- Login: minioadmin / minioadmin
- Browse bucket `files`
- You should see your uploaded file

---

## 2. Test Product Service

### Create a product
```powershell
$userId = [guid]::NewGuid()
$body = @{
    name = "Demo Product"
    description = "This is a test product"
} | ConvertTo-Json

$response = curl -X POST http://localhost:8084/api/products `
  -H "Content-Type: application/json" `
  -H "X-User-Id: $userId" `
  -d $body `
  -UseBasicParsing

$product = $response.Content | ConvertFrom-Json
$productId = $product.id
Write-Host "Product created with ID: $productId"
```

### Get product
```powershell
curl http://localhost:8084/api/products/$productId | ConvertFrom-Json
```

### Attach media (file reference)
```powershell
# First upload a file (see above), then attach it
$fileId = "YOUR_FILE_ID_HERE"
$productId = "YOUR_PRODUCT_ID_HERE"

$mediaBody = @{
    fileId = $fileId
    altText = "Product image"
    sortOrder = 1
    isPrimary = $true
} | ConvertTo-Json

curl -X POST http://localhost:8084/api/products/$productId/media `
  -H "Content-Type: application/json" `
  -d $mediaBody
```

---

## 3. Test Audit Service

### View audit logs
```powershell
curl http://localhost:8083/api/audit | ConvertFrom-Json
# You should see logs for FileUploaded and ProductCreated events
```

---

## 4. Test via Gateway

All services are accessible through the gateway at port 8081:

```powershell
# Files via Gateway
curl http://localhost:8081/api/files -H "X-User-Id: $(New-Guid)"

# Products via Gateway
curl http://localhost:8081/api/products -H "X-User-Id: $(New-Guid)"

# Audit via Gateway
curl http://localhost:8081/api/audit
```

---

## 5. Check Swagger UI

- **Files:** http://localhost:8082/swagger-ui.html
- **Audit:** http://localhost:8083/swagger-ui.html
- **Product:** http://localhost:8084/swagger-ui.html

---

## 6. View Metrics

### Prometheus
http://localhost:9090

Query examples:
- `up` - Check service status
- `jvm_memory_used_bytes` - Memory usage
- `http_server_requests_seconds_count` - Request count

### Grafana
http://localhost:3000 (admin/admin)

Note: Dashboards need to be imported manually (not yet created)

---

## 7. Check Kafka Topics

```powershell
# Access Kafka container
docker exec -it deploy-kafka-1 bash

# List topics
kafka-topics --bootstrap-server localhost:9092 --list

# Consume messages from files events
kafka-console-consumer --bootstrap-server localhost:9092 `
  --topic files.events.v1 --from-beginning

# Consume messages from products events
kafka-console-consumer --bootstrap-server localhost:9092 `
  --topic products.events.v1 --from-beginning
```

---

## 8. Check Database

### Files Database
```powershell
docker exec -it deploy-postgres-files-1 psql -U filesuser -d filesdb

# In psql:
\dt                    # List tables
SELECT * FROM file_entries;
\q                     # Quit
```

### Audit Database
```powershell
docker exec -it deploy-postgres-audit-1 psql -U audituser -d auditdb

SELECT * FROM audit_logs;
```

### Product Database
```powershell
docker exec -it deploy-postgres-product-1 psql -U productuser -d productdb

SELECT * FROM products;
SELECT * FROM variants;
SELECT * FROM media;
```

---

## Troubleshooting

### Services not starting?
```powershell
# Check logs
docker-compose -f deploy/docker-compose.yml logs -f

# Check specific service
docker-compose -f deploy/docker-compose.yml logs files-service
```

### Connection refused?
Wait a bit longer - services need time to start. Infrastructure services (Postgres, Kafka) should start first.

### File upload fails?
Check if MinIO bucket is created:
- http://localhost:9001
- Bucket `files` should exist
- If not, the S3 adapter creates it on first use
