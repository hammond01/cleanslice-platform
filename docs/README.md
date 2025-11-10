# CleanSlice Platform

A file-centric microservices platform built with Java 21, Spring Boot 3.3, Kafka, PostgreSQL, and MinIO.

## Architecture

This platform consists of three core services:

- **Files Service**: File upload, versioning, presigned URLs, sharing with TTL, soft delete/restore, tags/search, user quotas
- **Audit Service**: Persists audit logs for events from Files & Product services
- **Product Service**: Product/Variant/Media management with file references

Additional components:
- **Gateway Service**: Spring Cloud Gateway for routing and security
- **Identity**: Keycloak for authentication and authorization
- **Event Bus**: Kafka for inter-service communication
- **Storage**: MinIO/S3 for file storage
- **Observability**: Prometheus + Grafana for monitoring

## Tech Stack

- Java 21
- Spring Boot 3.3
- Maven (multi-module)
- PostgreSQL 16
- Kafka
- MinIO
- Keycloak
- Docker & Docker Compose

## Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (or use included `mvnw`)

### Run Locally (Fastest Way)

**Windows (PowerShell):**
```powershell
.\start.ps1
```

**Linux/Mac:**
```bash
make up
```

This will:
1. Build all services
2. Start infrastructure (Postgres, Kafka, MinIO, Keycloak)
3. Start application services (Gateway, Files, Audit, Product)

### Manual Steps

1. **Build services:**
   ```powershell
   .\mvnw.cmd clean package -DskipTests
   ```

2. **Start infrastructure first:**
   ```bash
   cd deploy
   docker compose up -d postgres-files postgres-audit postgres-product kafka zookeeper minio
   ```

3. **Wait 30 seconds, then start app services:**
   ```bash
   docker compose up -d gateway-service files-service audit-service product-service
   ```

4. **Access services:**
   - Gateway: http://localhost:8081
   - Files Swagger: http://localhost:8082/swagger-ui.html
   - Audit Swagger: http://localhost:8083/swagger-ui.html
   - Product Swagger: http://localhost:8084/swagger-ui.html
   - MinIO Console: http://localhost:9001 (minioadmin/minioadmin)
   - Grafana: http://localhost:3000 (admin/admin)

### Smoke Tests

**1. Upload a file:**
```powershell
$userId = [guid]::NewGuid()
curl -X POST http://localhost:8082/api/files `
  -H "X-User-Id: $userId" `
  -F "file=@test.png"
```

**2. Get download URL (302 redirect to presigned URL):**
```powershell
curl -i http://localhost:8082/api/files/{fileId}/download
```

**3. Create a product:**
```powershell
$userId = [guid]::NewGuid()
curl -X POST http://localhost:8084/api/products `
  -H "Content-Type: application/json" `
  -H "X-User-Id: $userId" `
  -d '{"name":"Demo Product","description":"Test product"}'
```

**4. View audit logs:**
```powershell
curl http://localhost:8083/api/audit
```

### Stop Services

**Windows:**
```powershell
.\stop.ps1
```

**Linux/Mac:**
```bash
make down
```

## Development

### Project Structure

```
cleanslice-platform/
├── common-libs/          # Shared libraries
│   ├── common-core/      # Core utilities
│   ├── common-infra/     # Infrastructure utilities
│   ├── common-events/    # Event definitions
│   └── common-test/      # Test utilities
├── services/             # Microservices
│   ├── gateway-service/  # API Gateway
│   ├── identity/         # Keycloak config
│   ├── files-service/    # File management
│   │   ├── files-domain/
│   │   ├── files-application/
│   │   ├── files-adapters-in-rest/
│   │   ├── files-adapters-out-jpa/
│   │   ├── files-adapters-out-s3/
│   │   └── files-adapters-out-kafka/
│   ├── audit-service/    # Audit logging
│   └── product-service/  # Product management
├── deploy/               # Deployment configs
│   ├── docker-compose.yml
│   └── app.env
└── docs/                 # Documentation
```

### Building

```bash
mvn clean compile
```

### Testing

```bash
mvn test
```

### Code Quality

- Jacoco coverage: ≥70% for Files & Product services
- Spotless for code formatting
- Checkstyle for code style

## API Documentation

Each service exposes OpenAPI/Swagger UI at `/swagger-ui.html`.

### Files API

- `POST /api/files` - Upload file
- `GET /api/files/{id}/download` - Get presigned download URL
- `DELETE /api/files/{id}` - Soft delete
- `POST /api/files/{id}/restore` - Restore file
- `POST /api/files/{id}/share` - Create share link
- `GET /api/files:search` - Search files

### Audit API

- `GET /api/audit` - Query audit logs

### Product API

- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `POST /api/products/{id}/variants` - Add variant
- `POST /api/products/{id}/media` - Attach media (file reference)
- `GET /api/products/{id}` - Get product
- `GET /api/products` - Search products

## Security

- JWT-based authentication via Keycloak
- Resource Server configuration in each service
- Owner-only access for file operations
- `files.write` and `products.write` scopes

## Observability

- Micrometer metrics for key operations
- OpenTelemetry tracing
- Prometheus scraping
- Grafana dashboards

## Contributing

1. Follow conventional commits
2. Write tests for new features
3. Update documentation
4. Ensure code coverage ≥70%

## License

[Add license information]