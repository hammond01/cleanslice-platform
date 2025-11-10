# CLEANSlice Micro (Java + Maven) — Agent Prompt

**Your role:** Senior Java Architect & Implementer. Generate a fully runnable codebase that follows the exact specification below, including tests, Docker, and documentation.

---

## 0) Goal & Scope

Build a **file-centric** system with three core services:

- **files-service**: file upload, simple versioning, **presigned URL** (MinIO/S3), share link with TTL, soft delete/restore, tags/basic search, user quota.
- **audit-service**: persist **AuditLog** for events emitted by Files & Product.
- **product-service**: Product/Variant/Media (media references `fileId` from Files), event publishing.

Also include:

- **gateway-service** (Spring Cloud Gateway),
- **identity** (Keycloak; JWT/OIDC resource server for each service),
- **Kafka** (event bus), **Postgres**, **MinIO**, **Prometheus + Grafana**, **OpenTelemetry**.

Deliver **two run modes** from the same multi-module Maven repo:

1. **Modular Monolith profile** (single app for local learning)
2. **Microservices profile** (services split, communicating via REST + Kafka)

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

## 2) Maven Structure (multi-module)

Create a repository: `cleanslice-micro/`

```
cleanslice-micro/
  pom.xml                       # parent (dependencyManagement, pluginManagement)
  common-libs/
    common-core/
    common-infra/
    common-events/
    common-test/
  services/
    gateway-service/
    identity/                   # keycloak realm export + helper (no server code)
    files-service/
      files-domain/
      files-application/
      files-adapters-in-rest/
      files-adapters-out-jpa/
      files-adapters-out-s3/
      files-adapters-out-kafka/
    audit-service/
      audit-domain/
      audit-application/
      audit-adapters-in-rest/
      audit-adapters-out-jpa/
      audit-adapters-in-kafka/
    product-service/
      product-domain/
      product-application/
      product-adapters-in-rest/
      product-adapters-out-jpa/
      product-adapters-out-kafka/
  deploy/
    docker-compose.yml
    k8s/                        # optional (can be empty)
    app.env
  docs/
    README.md
    ADR-001-presigned-url.md
    ADR-002-vertical-slice-vs-3layer.md
    ADR-003-modular-monolith-to-microservices.md
```

**Rules:** Hexagonal + Vertical Slice inside each service. No adapter calls another adapter directly. DTOs are separate from Entities. Transactions at use-case level. Validation via Bean Validation. Errors mapped to **RFC 7807 ProblemDetail** JSON. Use conventional commits (document in CONTRIBUTING.md).

---

## 3) Core Domains & Use Cases

### Files Service

- **Entities**: `FileEntry`, `FileVersion`, `ShareLink`, `UserQuota`
- **Ports**: `FileStoragePort`, `FileEntryRepositoryPort`, `ShareLinkRepositoryPort`, `QuotaServicePort`
- **Use-cases**: `UploadFile`, `GetDownloadUrl`, `DeleteFile (soft)`, `RestoreFile`, `ShareFile`, `RevokeShare`, `SearchFiles`
- **Events (Kafka)**: `FileUploadedEvent`, `FileDeletedEvent`, `FileRestoredEvent`, `FileSharedEvent`, `FileShareRevokedEvent`

### Audit Service

- **Entity**: `AuditLog`
- **Consume** Kafka events from Files & Product; persist logs
- **REST**: `GET /api/audit?actor&action&resource&from&to`

### Product Service

- **Entities**: `Product`, `Variant`, `Media`, `Category`
- Media stores only `fileId` (image) from Files
- **Use-cases**: `CreateProduct (+ default variant)`, `UpdateProduct`, `AddVariant`, `AttachMedia(fileId)`, `PublishProduct`, `GetProduct`, `SearchProducts`
- **Events (Kafka)**: `ProductCreated`, `ProductUpdated`, `ProductPublished`, `VariantAdded`, `ProductMediaAttached`

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

## 14) Immediate Output from the Agent

1. The **full directory tree and all POM files** (parent + module poms) in the correct build order
2. **Bootstrapped code** for:
   - Ports + entities + core handlers (UploadFile, GetDownloadUrl, CreateProduct, AttachMedia)
   - REST controllers per the OpenAPI draft
   - S3 adapter, JPA repositories, Kafka producer/consumer skeletons
   - Security config (Resource Server), `mock` profile
3. **docker-compose.yml** and `application.yaml` samples per service (`dev`, `mock` profiles)
4. **Testcontainers** examples for Files repository and Audit Kafka consumer
5. **README.md (EN)** skeleton and **ADR** skeletons
6. **GitHub Actions** workflow `ci.yml`

**Guidelines:** Prioritize a minimal runnable system first. Keep adapters thin and handlers explicit. Comment important trade-offs briefly.

---

## 15) Quick Start (what to print at the end)

**Run locally (dev):**

1. `docker compose -f deploy/docker-compose.yml up -d`
2. `mvn -q -T 1C -DskipTests package && docker compose -f deploy/docker-compose.yml up -d --build`
3. Open Swagger at `http://localhost:<gateway-port>/swagger` (list per-service UIs)

**Smoke tests (curl):**

1. `curl -F "file=@/path/image.png" http://localhost:<gateway-port>/api/files`
2. `curl -i http://localhost:<gateway-port>/api/files/{id}/download` (expect **302**)
3. `curl -X POST -H "Content-Type: application/json" -d '{"name":"Demo Product"}' http://localhost:<gateway-port>/api/products`
