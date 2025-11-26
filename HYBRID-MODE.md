# 🚀 Quick Start - Hybrid Mode (Infrastructure Docker + Services Windows)

Hướng dẫn chạy CleanSlice Platform với:
- **Infrastructure** (Postgres, Kafka, Keycloak, MinIO) chạy trên **Docker**
- **Services** (Gateway, Identity, Files, Product, Audit) chạy trên **Windows**

---

## 📋 Prerequisites

1. **Docker Desktop** đang chạy
2. **Java 21** đã cài đặt
3. **PowerShell** (Windows built-in)
4. **Các ports sau phải available:**
   - 8080-8085 (Services + Keycloak)
   - 5432 (PostgreSQL)
   - 9000-9001 (MinIO)
   - 9090-9092 (Prometheus + Kafka)
   - 2181 (Zookeeper)
   - 3000 (Grafana)

---

## 🎯 Quick Start (3 Commands)

### 1️⃣ Start Infrastructure + Build + Start Services
```powershell
.\start-hybrid.ps1
```

Điều này sẽ:
- ✅ Start Docker containers (Postgres, Kafka, Keycloak, MinIO, Prometheus, Grafana)
- ✅ Build tất cả services
- ✅ Start mỗi service trong window riêng biệt
- ✅ Health check tất cả services

### 2️⃣ Run API Tests
```powershell
.\test-api.ps1
```

Điều này sẽ test:
- ✅ Register user trong Keycloak
- ✅ Login và lấy JWT token
- ✅ Upload file lên MinIO
- ✅ Create product
- ✅ Attach media to product
- ✅ Verify audit logs

### 3️⃣ Stop Everything
```powershell
# 1. Đóng tất cả PowerShell windows (services)
# 2. Stop infrastructure:
cd deploy
docker-compose down
```

---

## 📝 Manual Steps (Nếu muốn control từng bước)

### Step 1: Start Infrastructure Only
```powershell
cd deploy
docker-compose up -d postgres kafka zookeeper minio keycloak postgres-keycloak prometheus grafana
cd ..
```

### Step 2: Build Services
```powershell
./mvnw.cmd clean package -DskipTests
```

### Step 3: Start Each Service Manually

**Gateway Service:**
```powershell
java -jar services/gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar
```

**Identity Service:**
```powershell
java -jar services/identity/target/identity-1.0.0-SNAPSHOT.jar
```

**Files Service:**
```powershell
java -jar services/files-service/target/files-service-1.0.0-SNAPSHOT.jar
```

**Product Service:**
```powershell
java -jar services/product-service/target/product-service-1.0.0-SNAPSHOT.jar
```

**Audit Service:**
```powershell
java -jar services/audit-service/audit-adapters-in-rest/target/audit-service-1.0.0-SNAPSHOT.jar
```

---

## 🌐 Access URLs

### API Services
- **Gateway:** http://localhost:8081
- **Identity:** http://localhost:8085
- **Files:** http://localhost:8082
- **Product:** http://localhost:8084
- **Audit:** http://localhost:8083

### Swagger UI
- **Identity:** http://localhost:8085/swagger-ui.html
- **Files:** http://localhost:8082/swagger-ui.html
- **Product:** http://localhost:8084/swagger-ui.html
- **Audit:** http://localhost:8083/swagger-ui.html

### Infrastructure
- **Keycloak Admin:** http://localhost:8080 (admin/admin)
- **MinIO Console:** http://localhost:9001 (minioadmin/minioadmin)
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000 (admin/admin)

---

## 🧪 Manual API Testing

### 1. Register User
```powershell
curl -X POST http://localhost:8085/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"john\",\"email\":\"john@example.com\",\"password\":\"Pass123!\"}'
```

### 2. Login (Get JWT Token)
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8085/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"john","password":"Pass123!"}'

$token = $response.token
Write-Host "Token: $token"
```

### 3. Upload File
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

$form = @{
    file = Get-Item -Path "test.txt"
}

$response = Invoke-RestMethod -Uri "http://localhost:8082/api/files" `
  -Method POST `
  -Headers $headers `
  -Form $form

$fileId = $response.fileId
Write-Host "File ID: $fileId"
```

### 4. Create Product
```powershell
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$body = @{
    name = "Product A"
    description = "Test product"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8084/api/products" `
  -Method POST `
  -Headers $headers `
  -Body $body

$productId = $response.id
Write-Host "Product ID: $productId"
```

### 5. Attach Media to Product
```powershell
$body = @{
    fileId = $fileId
    altText = "Product image"
    isPrimary = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8084/api/products/$productId/media" `
  -Method POST `
  -Headers $headers `
  -Body $body
```

---

## 🔍 Troubleshooting

### Services không start
1. Check logs trong PowerShell window của service đó
2. Verify ports không bị chiếm: `netstat -ano | findstr "8081 8082 8083 8084 8085"`

### Keycloak connection issues
1. Check Keycloak running: http://localhost:8080
2. Đợi thêm vài giây cho Keycloak khởi động hoàn toàn
3. Check realm imported: Login Keycloak Admin > Select "cleanslice" realm

### Database connection issues
1. Check PostgreSQL running: `docker ps | findstr postgres`
2. Check connection: `docker exec -it deploy-postgres-1 psql -U postgres -c "\l"`

### Kafka connection issues
1. Check Kafka running: `docker ps | findstr kafka`
2. Check logs: `docker logs deploy-kafka-1`

---

## 📊 Monitoring

### Prometheus Metrics
```
http://localhost:9090/targets
```

### Grafana Dashboards
1. Login: http://localhost:3000 (admin/admin)
2. Add Prometheus datasource: http://prometheus:9090
3. Import dashboards cho Spring Boot metrics

### Application Logs
- Mỗi service có logs trong PowerShell window riêng
- Hoặc check logs: `java -jar ... > service.log 2>&1`

---

## 🛑 Cleanup

### Stop Services
Đóng tất cả PowerShell windows (Ctrl+C hoặc close window)

### Stop Infrastructure
```powershell
cd deploy
docker-compose down
```

### Clean Everything (Including Volumes)
```powershell
cd deploy
docker-compose down -v
```

---

## 📚 Next Steps

- ✅ Add role-based access control (@PreAuthorize)
- ✅ Write integration tests
- ✅ Setup Grafana dashboards
- ✅ Configure distributed tracing
- ✅ Add resilience patterns (Circuit Breaker, Retry)

---

## 🐛 Known Issues

1. **Port conflicts:** Ensure no other apps using ports 8080-8085
2. **Docker memory:** Increase Docker memory to at least 4GB
3. **Windows Firewall:** May need to allow Java apps

---

## 💡 Tips

1. **Fast restart:** Không cần rebuild nếu chỉ thay đổi config
2. **Debug mode:** Add `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005` to java command
3. **Profile switching:** Add `--spring.profiles.active=dev` for different configs
