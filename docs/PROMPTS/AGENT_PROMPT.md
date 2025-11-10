# CLEANSlice Platform (Java + Maven) — Agent Prompt

**Your role:** Senior Java Architect & Implementer. Generate a fully runnable microservices platform that follows clean architecture principles, practical design patterns, and production-ready best practices.

---

## 0) Goal & Scope

Build a **file-centric microservices platform** with three core services:

- **files-service**: file upload, presigned URL (MinIO/S3), share link with TTL, soft delete/restore, tags/search, user quota.
- **audit-service**: persist **AuditLog** for events emitted by Files & Product (uses Hexagonal Architecture for learning).
- **product-service**: Product/Variant/Media management (media references `fileId` from Files).

Supporting services:

- **gateway-service** (Spring Cloud Gateway for API routing)
- **identity** (Keycloak for JWT/OIDC authentication)

Infrastructure:

- **Kafka** (event-driven messaging)
- **PostgreSQL** (separate database per service)
- **MinIO** (S3-compatible storage)
- **Prometheus + Grafana** (observability)

**Architecture Philosophy:**
- **Audit Service**: Full Hexagonal Architecture (multi-module) for educational purposes
- **Product & Files Services**: Simplified single-module structure for production practicality
- **Balance between academic purity and real-world pragmatism**

---

## 1) Tech Stack (use these versions or latest stable in the same major)

- **Java 21**, **Maven** (multi-module)
- **Spring Boot 3.3.x**, Spring Web, Spring Security (Resource Server, JWT), Validation
- **Spring Data JPA + PostgreSQL 16**
- **AWS SDK v2** + **MinIO** (presigned URLs for downloads)
- **Kafka** (spring-kafka)
- **OpenAPI** via springdoc-openapi 2.x
- **Observability**: Micrometer + Prometheus, **OpenTelemetry** (OTLP)
- **Testing**: JUnit 5, AssertJ, **Testcontainers** (Postgres, Kafka, MinIO, Keycloak)
- **CI**: GitHub Actions

---

## 2) Maven Structure (Hybrid Multi-Module)

Repository: `cleanslice-platform/`

```
cleanslice-platform/
  pom.xml                       # parent (dependencyManagement, pluginManagement)
  
  common-libs/                  # Shared libraries
    common-core/                # Base utilities, exceptions
    common-infra/               # Infrastructure utilities
    common-events/              # Domain event definitions
    common-test/                # Test utilities
  
  services/
    gateway-service/            # Single module (Spring Cloud Gateway)
      pom.xml
      src/main/java/...gateway/
      Dockerfile
    
    identity/                   # Keycloak realm config (POM only)
      pom.xml
    
    files-service/              # ⚡ SIMPLIFIED: Single module
      pom.xml
      src/main/java/.../files/
        domain/                 # Package: entities, ports
        application/            # Package: use cases
        infrastructure/         # Package: REST, JPA, S3, Kafka adapters
          rest/
          persistence/
          storage/
          messaging/
      src/main/resources/
      Dockerfile
    
    product-service/            # ⚡ SIMPLIFIED: Single module  
      pom.xml
      src/main/java/.../product/
        domain/                 # Package: entities, value objects
        application/            # Package: use cases, ports
        infrastructure/         # Package: all adapters
          rest/
          persistence/
          messaging/
      src/main/resources/
      Dockerfile
    
    audit-service/              # 📚 MULTI-MODULE: For learning Hexagonal Architecture
      audit-domain/             # Pure domain (no framework deps)
      audit-application/        # Use cases (orchestration)
      audit-adapters-in-rest/   # REST API + Spring Boot main
      audit-adapters-in-kafka/  # Kafka event consumers
      audit-adapters-out-jpa/   # PostgreSQL persistence
      Dockerfile
  
  deploy/
    docker-compose.yml
    app.env
    prometheus.yml
    k8s/                        # Optional Kubernetes manifests
  
  docs/
    README.md
    ADR-001-presigned-url.md
    ADR-002-vertical-slice-vs-3layer.md
    ADR-003-modular-monolith-to-microservices.md
    PROMPTS/
      AGENT_PROMPT.md
```

**Architecture Principles:**

1. **Audit Service (Multi-Module)**: Demonstrates pure Hexagonal Architecture
   - Domain layer has ZERO infrastructure dependencies
   - Adapters are separate Maven modules
   - Perfect for teaching/learning clean architecture

2. **Product & Files Services (Single-Module)**: Pragmatic package-based structure
   - Faster builds, easier navigation
   - Still maintains clean architecture via package naming
   - Better for real-world productivity

3. **Common Rules:**
   - No adapter calls another adapter directly
   - DTOs separate from domain entities
   - Transactions at use-case level
   - Bean Validation for input validation
   - RFC 7807 ProblemDetail for error responses
   - Conventional commits for version control

---

## 3) Core Domains & Use Cases

### Files Service (Single Module with Package Structure)

**Domain Package** (`domain/`):
- **Entities**: `FileEntry`, `ShareLink` (domain models, NOT JPA entities)
- **Value Objects**: `FileMetadata`, `ShareToken`
- **Ports**: `FileStoragePort`, `FileEntryRepositoryPort`, `ShareLinkRepositoryPort`, `QuotaServicePort`

**Application Package** (`application/`):
- **Use-cases**: `UploadFileUseCase`, `GetDownloadUrlUseCase`, `DeleteFileUseCase`, `RestoreFileUseCase`, `ShareFileUseCase`, `RevokeShareUseCase`, `SearchFilesUseCase`

**Infrastructure Package** (`infrastructure/`):
- **REST** (`infrastructure/rest/`): `FilesController`, `FilesServiceApplication`, `SecurityConfig`
- **Persistence** (`infrastructure/persistence/`): `FileEntryEntity` (JPA), `FileEntryRepositoryAdapter`, `JpaFileEntryRepository`
- **Storage** (`infrastructure/storage/`): `S3FileStorageAdapter` (MinIO/S3 SDK)
- **Messaging** (`infrastructure/messaging/`): `KafkaEventPublisher`

**Events (Kafka)**: 
- Topic: `files.events.v1`
- Events: `FileUploadedEvent`, `FileDeletedEvent`, `FileRestoredEvent`, `FileSharedEvent`

---

### Audit Service (Multi-Module Hexagonal Architecture)

**audit-domain/**:
- **Entity**: `AuditLog` (pure POJO, no JPA annotations)
- **NO dependencies** on Spring or infrastructure frameworks

**audit-application/**:
- Typically empty (audit is simple event persistence)
- Could contain complex audit queries/aggregations if needed

**audit-adapters-in-kafka/**:
- Kafka consumers listening to `files.events.v1` and `products.events.v1`
- Convert events to AuditLog domain objects

**audit-adapters-in-rest/**:
- REST API: `GET /api/audit?actor&action&resource&from&to`
- Spring Boot main application class
- Security configuration

**audit-adapters-out-jpa/**:
- `AuditLogEntity` (JPA entity with annotations)
- `AuditLogRepository` (Spring Data JPA)
- Mapper: `AuditLog` (domain) ↔ `AuditLogEntity` (persistence)

---

### Product Service (Single Module with Package Structure)

**Domain Package** (`domain/`):
- **Entities**: `Product`, `Variant`, `Media` (business logic, NOT JPA entities)
- **Value Objects**: `ProductStatus`, `Price`
- Media stores only `fileId` (reference to Files service)

**Application Package** (`application/`):
- **Ports**: `ProductRepositoryPort`, `EventPublisherPort`
- **Use-cases**: 
  - `CreateProductUseCase` (+ auto-create default variant)
  - `UpdateProductUseCase`
  - `AddVariantUseCase`
  - `AttachMediaUseCase` (fileId validation)
  - `PublishProductUseCase`
  - `GetProductUseCase`
  - `SearchProductsUseCase`

**Infrastructure Package** (`infrastructure/`):
- **REST** (`infrastructure/rest/`): `ProductController`, `ProductServiceApplication`, `SecurityConfig`
- **Persistence** (`infrastructure/persistence/`): `ProductEntity`, `VariantEntity`, `MediaEntity` (JPA), `ProductRepositoryAdapter`
- **Messaging** (`infrastructure/messaging/`): `KafkaEventPublisher`

**Events (Kafka)**:
- Topic: `products.events.v1`
- Events: `ProductCreatedEvent`, `ProductUpdatedEvent`, `ProductPublishedEvent`, `VariantAddedEvent`, `MediaAttachedEvent`

---

## 4) REST API (OpenAPI-first)

**Files**

- `POST /api/files` (multipart) → `{fileId, versionId}`
- `GET /api/files/{id}` → metadata
- `GET /api/files/{id}/download` → **302** redirect to presigned URL
- `DELETE /api/files/{id}` → soft delete
- `POST /api/files/{id}/restore`
- `POST /api/files/{id}/share` → `{token, url, expiresAt}`
- `DELETE /api/files/{id}/share/{token}`
- `GET /api/files:search?name&tag&type&page&size`

**Audit**

- `GET /api/audit?actor&action&resource&from&to`

**Product**

- `POST /api/products`
- `PUT /api/products/{id}`
- `POST /api/products/{id}/variants`
- `PUT /api/variants/{id}`
- `POST /api/products/{id}/media` (body: `fileId, altText, sortOrder, isPrimary`)
- `POST /api/products/{id}:publish`
- `GET /api/products/{id}`
- `GET /api/products?query&category&page&size`

**Download controller** must return a 302 to the presigned URL (do not stream the binary).

---

## 5) Security

- **Keycloak** realm (export file) with a client `gateway` and scopes:  
  `files.read`, `files.write`, `products.read`, `products.write`.
- Each service is a **Spring Security Resource Server** (JWT validation).
- **Owner-only** policy for Files mutations; `products.write` for Product mutations.
- Provide a `mock` profile to bypass OIDC for offline demos (stub JWT).

---

## 6) Messaging (Kafka)

- Topics:
  - `files.events.v1` (key: `fileId`, payload contains `type`)
  - `products.events.v1` (key: `productId`)
- JSON schema in `common-events` (simple and explicit). Example:

```json
{
  "type": "FileUploadedEvent",
  "occurredAt": "2025-11-10T10:00:00Z",
  "fileId": "uuid",
  "ownerId": "uuid",
  "versionId": "uuid",
  "size": 12345,
  "contentType": "image/png"
}
```

- Ensure producer/consumer idempotency (keys, retries, DLQ optional).

---

## 7) Storage (MinIO/S3)

- `FileStoragePort` with S3 implementation: `put`, `get`, `delete`, `presignedReadUrl(ttl)`
- Use **MinIO** locally; configure bucket via environment variables
- Never expose `storageKey` outside the service

---

## 8) Observability & Health

- **Micrometer** metrics (per use-case):
  - `files_upload_seconds`, `files_download_redirect_ms`, `files_quota_usage_bytes`
  - `product_write_seconds`, `product_search_seconds`, `product_media_attach_seconds`
- **OpenTelemetry**: create spans for each handler; propagate `traceparent` via Kafka headers
- **Actuator**: health/readiness/liveness, info
- Provide **Prometheus + Grafana** dashboards (a basic JSON to import)

---

## 9) Docker Compose (dev)

Create `deploy/docker-compose.yml` that starts:

- **postgres** (separate DBs for files, audit, product)
- **minio** (+ console)
- **kafka + zookeeper**
- **keycloak** (import realm)
- **prometheus + grafana**
- **gateway-service**, **files-service**, **audit-service**, **product-service**

Provide `app.env` and a **Makefile** (or `mvnw` scripts):

- `make up`, `make down`, `make logs`, `make seed`, `make test`

---

## 10) Testing

- **Unit**: domain invariants (quota, share TTL, product media primary)
- **Integration**: JPA + MinIO + Kafka (Testcontainers)
- **Contract**: OpenAPI snapshot / API diff — fail on breaking changes
- **Coverage**: Jacoco ≥ **70%** for Files & Product modules

---

## 11) CI (GitHub Actions)

Workflows:

- `build-test`: JDK 21, cache Maven, `mvn -B verify` (with Testcontainers)
- `docker`: build/publish images for services
- Upload Jacoco (badge) and test reports

---

## 12) Documentation (English)

Generate:

- `docs/README.md`: architecture, run locally, diagrams, screenshots
- `docs/CASE-STUDY.md`: Problem → Architecture → Security → Observability → Results (P95) → Trade-offs
- `docs/ADR-001..003.md`
- A **30–45s GIF**: Upload → Share → Download (presigned) → Revoke; Attach media for Product → display image via Files

---

## 13) Acceptance Criteria (must-pass)

- `docker compose up -d` brings the whole stack **UP**; Swagger UIs accessible via **Gateway**
- Upload 5MB: **P95 ≤ 3s** (local)
- Download redirect (presigned): **P95 ≤ 150ms**
- Product create + attach one image: **P95 ≤ 300ms**
- Kafka events flow: Audit receives `FileUploaded` and `ProductCreated`
- Testcontainers pass; Jacoco coverage ≥ **70%** for Files & Product
- README (EN) with **Grafana screenshots** and a **trace screenshot**

---

## 14) Key Architectural Decisions

### Why Different Structures for Different Services?

**Audit Service (Multi-Module Hexagonal)**:
- **Purpose**: Educational/demonstration of pure clean architecture
- **Benefits**: Shows proper dependency inversion, domain isolation
- **Trade-off**: More complexity, but valuable for learning

**Product & Files Services (Single-Module Package-Based)**:
- **Purpose**: Production-ready pragmatic approach
- **Benefits**: Faster builds, easier navigation, less overhead
- **Trade-off**: Requires team discipline (no compiler enforcement of dependencies)

**When to use Multi-Module:**
- Large services (>50 classes)
- Multiple teams working on same service
- High reusability requirements
- Learning/teaching clean architecture

**When to use Single-Module:**
- Small to medium services (<50 classes)
- Single team ownership
- Need for development velocity
- Production pragmatism

---

## 15) Migration Notes

**From Multi-Module to Single-Module:**

```bash
# Example: Product Service migration
product-service/
├── product-domain/src/main/java/...          → src/main/java/.../domain/
├── product-application/src/main/java/...     → src/main/java/.../application/
├── product-adapters-in-rest/src/main/java/...    → src/main/java/.../infrastructure/rest/
├── product-adapters-out-jpa/src/main/java/...    → src/main/java/.../infrastructure/persistence/
└── product-adapters-out-kafka/src/main/java/...  → src/main/java/.../infrastructure/messaging/

# Dependencies merge: All POM dependencies → single pom.xml
# Package imports: Update to new package structure
# Keep: Domain entities as POJOs (remove JPA if present)
# Create: Separate persistence entities with JPA annotations
```

**Audit Service stays multi-module** - no migration needed!

---

## 16) Immediate Output from the Agent

1. **Full directory tree** for hybrid structure (audit multi-module, others single-module)
2. **POM files** with correct dependencies for each structure type
3. **Bootstrapped code**:
   - Files Service: Single module with domain/application/infrastructure packages
   - Product Service: Single module with domain/application/infrastructure packages
   - Audit Service: Multi-module hexagonal (educational example)
4. **Separation of concerns**:
   - Domain models (POJOs) vs Persistence entities (JPA) for Files & Product
   - Pure domain (no JPA) in Audit domain module
5. **docker-compose.yml** and profile configurations
6. **Testcontainers** examples for both structures
7. **README.md** explaining the hybrid architecture approach
8. **ADR-004**: Document why hybrid structure was chosen

---

## 17) Quick Start

**Run locally (dev):**

```bash
# Start infrastructure
cd deploy
docker-compose up -d postgres-files postgres-audit postgres-product kafka zookeeper minio

# Build all services
cd ..
./mvnw clean package -DskipTests

# Start application services
cd deploy
docker-compose up -d gateway-service files-service audit-service product-service

# Or use convenience scripts
./start.ps1   # Windows
make up       # Linux/Mac
```

**Test Product Service standalone (no Docker):**
```bash
./run-product-local.ps1  # Uses H2 in-memory database
./test-product-api.ps1   # Smoke tests
```

**Access:**
- Gateway: http://localhost:8081
- Files Swagger: http://localhost:8082/swagger-ui.html
- Audit Swagger: http://localhost:8083/swagger-ui.html  
- Product Swagger: http://localhost:8084/swagger-ui.html
- MinIO Console: http://localhost:9001
- Grafana: http://localhost:3000

**Smoke tests:**
```bash
# Upload file
curl -X POST http://localhost:8082/api/files \
  -H "X-User-Id: $(uuidgen)" \
  -F "file=@test.png"

# Get download URL (302 redirect to presigned URL)
curl -i http://localhost:8082/api/files/{fileId}/download

# Create product
curl -X POST http://localhost:8084/api/products \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $(uuidgen)" \
  -d '{"name":"Demo Product","description":"Test"}'

# View audit logs
curl http://localhost:8083/api/audit
```
