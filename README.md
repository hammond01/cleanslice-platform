# CleanSlice Platform

A file-centric microservices platform with hexagonal architecture.

## 🚀 Quick Start (Windows)

```powershell
.\start.ps1
```

Then start Identity Service in a separate terminal:
```powershell
.\run-identity-local.ps1
```

Then access:
- **API Gateway:** http://localhost:8081
- **Identity Service:** http://localhost:8085/swagger-ui.html
- **Files Service:** http://localhost:8082/swagger-ui.html
- **Audit Service:** http://localhost:8083/swagger-ui.html  
- **Product Service:** http://localhost:8084/swagger-ui.html
- **Keycloak Admin:** http://localhost:8080 (admin/admin)
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

### Identity Service (Port 8085)
- User registration and authentication
- JWT token generation and validation
- User management

### Files Service (Port 8082)
- Upload files to MinIO/S3
- Generate presigned download URLs
- File versioning & metadata
- Soft delete/restore

#### File Versioning (examples)
You can work with file versions using the Files Service API:

- Upload a file to create a version (or a new one if the file exists):
```powershell
curl -X POST http://localhost:8082/api/files \
  -F "file=@test.txt"
```

- List available versions for a file:
```powershell
curl http://localhost:8082/api/files/{fileId}/versions
```

- Get metadata for a specific version:
```powershell
curl http://localhost:8082/api/files/version/{versionId}
```

- Get presigned download URL for a specific version:
```powershell
curl -i http://localhost:8082/api/files/version/{versionId}/download
```

- Restore a file to a previous version:
```powershell
curl -X POST http://localhost:8082/api/files/{fileId}/restore/{versionNumber}
```


### Audit Service (Port 8083)
- Consume events from Kafka
- Store audit logs
- Query audit history

### Product Service (Port 8084)
- Product catalog management
- Variants & media references
- Link product media to files

## 🧪 Test APIs

**Register a new user:**
```powershell
curl -X POST http://localhost:8085/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

**Login to get JWT token:**
```powershell
curl -X POST http://localhost:8085/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"testuser","password":"password123"}'
# Returns: {"token":"jwt_token_here","tokenType":"Bearer"}
```

**Upload a file (using JWT token):**
```powershell
$token = "your_jwt_token_here"
curl -X POST http://localhost:8082/api/files `
  -H "Authorization: Bearer $token" `
  -F "file=@test.png"
```

**Get presigned download URL:**
```powershell
curl -i http://localhost:8082/api/files/{fileId}/download `
  -H "Authorization: Bearer $token"
# Returns 302 redirect to MinIO presigned URL
```

**Create product:**
```powershell
curl -X POST http://localhost:8084/api/products `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
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
