# CleanSlice Platform

A file-centric microservices platform with hexagonal architecture.

## 🚀 Quick Start (Windows)

```powershell
.\start.ps1
```

Then access:
- **API Gateway:** http://localhost:8081
- **Files Service:** http://localhost:8082/swagger-ui.html
- **Audit Service:** http://localhost:8083/swagger-ui.html  
- **Product Service:** http://localhost:8084/swagger-ui.html
- **MinIO Console:** http://localhost:9001 (minioadmin/minioadmin)
- **Grafana:** http://localhost:3000 (admin/admin)

## 📁 Project Structure

```
cleanslice-platform/
├── common-libs/          # Shared libraries
│   ├── common-core/      
│   ├── common-events/    # Event definitions
│   ├── common-infra/     
│   └── common-test/      
├── services/             
│   ├── gateway-service/  # Spring Cloud Gateway
│   ├── files-service/    # File management with MinIO/S3
│   ├── audit-service/    # Event auditing
│   └── product-service/  # Product catalog
├── deploy/               
│   ├── docker-compose.yml
│   └── prometheus.yml
└── docs/                 # Detailed documentation
```

## 🎯 Core Services

### Files Service (Port 8082)
- Upload files to MinIO/S3
- Generate presigned download URLs
- File versioning & metadata
- Soft delete/restore

### Audit Service (Port 8083)
- Consume events from Kafka
- Store audit logs
- Query audit history

### Product Service (Port 8084)
- Product catalog management
- Variants & media references
- Link product media to files

## 🧪 Test APIs

**Upload a file:**
```powershell
curl -X POST http://localhost:8082/api/files `
  -H "X-User-Id: $(New-Guid)" `
  -F "file=@test.png"
```

**Get presigned download URL:**
```powershell
curl -i http://localhost:8082/api/files/{fileId}/download
# Returns 302 redirect to MinIO presigned URL
```

**Create product:**
```powershell
curl -X POST http://localhost:8084/api/products `
  -H "Content-Type: application/json" `
  -H "X-User-Id: $(New-Guid)" `
  -d '{"name":"Test Product","description":"Description"}'
```

**View audit logs:**
```powershell
curl http://localhost:8083/api/audit
```

## 🛠️ Tech Stack

- **Java 21** with Spring Boot 3.5
- **PostgreSQL** (separate DB per service)
- **Apache Kafka** (event bus)
- **MinIO/S3** (object storage with presigned URLs)
- **Spring Cloud Gateway** (API gateway)
- **Prometheus + Grafana** (observability)
- **Docker Compose** (local deployment)

## 📚 Architecture

- **Hexagonal Architecture** (Ports & Adapters)
- **Domain-Driven Design** principles
- **Event-Driven** inter-service communication
- **Database-per-Service** pattern

## 🛑 Stop Services

```powershell
.\stop.ps1
```

## 📖 Documentation

See [docs/README.md](docs/README.md) for detailed documentation including:
- Architecture decisions (ADRs)
- API specifications
- Development guide
- Deployment instructions

## 📝 License

[Add your license here]
