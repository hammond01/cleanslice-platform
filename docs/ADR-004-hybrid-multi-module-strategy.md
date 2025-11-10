# ADR-004: Hybrid Multi-Module Strategy

**Status:** Accepted  
**Date:** 2025-11-10  
**Context:** Microservices Architecture Design  
**Decision Makers:** Architecture Team

---

## Context

When structuring microservices with Hexagonal Architecture (Ports & Adapters), we face a trade-off:

### Option A: Full Multi-Module per Service
```
product-service/
├── product-domain/
├── product-application/
├── product-adapters-in-rest/
├── product-adapters-out-jpa/
└── product-adapters-out-kafka/
```

**Pros:**
- ✅ Compiler-enforced dependency rules
- ✅ Perfect separation of concerns
- ✅ Reusability (can extract domain module)
- ✅ Textbook clean architecture

**Cons:**
- ❌ 5-6 Maven modules per service = 15-18 modules total
- ❌ Slower builds (Maven reactor overhead)
- ❌ Development friction (touch multiple modules per feature)
- ❌ Over-engineering for simple services (<50 classes)

### Option B: Single Module with Package Structure
```
product-service/
└── src/main/java/.../product/
    ├── domain/
    ├── application/
    └── infrastructure/
        ├── rest/
        ├── persistence/
        └── messaging/
```

**Pros:**
- ✅ Faster builds and iteration
- ✅ Easier code navigation
- ✅ Less Maven overhead
- ✅ Pragmatic for small-medium services

**Cons:**
- ⚠️ No compiler enforcement (depends on team discipline)
- ⚠️ Risk of accidental coupling
- ⚠️ Less clear boundaries

---

## Decision

We adopt a **HYBRID STRATEGY**:

### 🎓 **Audit Service: Multi-Module (Educational)**
Keep full Hexagonal Architecture with separate Maven modules:
```
audit-service/
├── audit-domain/              # Pure domain (zero framework deps)
├── audit-application/         # Use cases
├── audit-adapters-in-kafka/   # Kafka consumers
├── audit-adapters-in-rest/    # REST API + Spring Boot
└── audit-adapters-out-jpa/    # PostgreSQL persistence
```

**Rationale:**
- Demonstrates **pure clean architecture** for learning/teaching
- Shows proper dependency inversion
- Domain layer has ZERO infrastructure dependencies
- Perfect example for onboarding new developers
- Portfolio/showcase value

---

### ⚡ **Product & Files Services: Single-Module (Pragmatic)**
Use package-based structure with discipline:
```
product-service/
└── src/main/java/.../product/
    ├── domain/                # Business entities (POJOs)
    │   ├── Product.java       # NO JPA annotations
    │   ├── Variant.java
    │   └── Media.java
    ├── application/           # Use cases + ports
    │   ├── port/
    │   │   └── ProductRepositoryPort.java
    │   └── usecase/
    │       ├── CreateProductUseCase.java
    │       └── AttachMediaUseCase.java
    └── infrastructure/        # All adapters
        ├── rest/
        │   └── ProductController.java
        ├── persistence/
        │   ├── ProductEntity.java      # WITH JPA annotations
        │   ├── ProductRepositoryAdapter.java
        │   └── JpaProductRepository.java
        └── messaging/
            └── KafkaEventPublisher.java
```

**Rationale:**
- Services are small (~10-20 classes each)
- Faster development velocity
- Less build overhead
- Easier for single-team ownership
- **Still maintains clean architecture via:**
  - Package naming conventions
  - Separation of domain POJOs vs persistence entities
  - Port/Adapter pattern in code structure
  - Code review enforcement

---

## Separation of Domain and Persistence Models

### ❌ **Problem: JPA Pollution in Domain**
```java
@Entity  // ← Infrastructure leak into domain!
@Table(name = "products")
public class Product {
    @Id
    private UUID id;
    
    @OneToMany(mappedBy = "product")  // ← JPA concern
    private List<Variant> variants;
}
```

### ✅ **Solution: Separate Models**

**Domain Model (Pure POJO):**
```java
// domain/Product.java
public class Product {
    private UUID id;
    private UUID ownerId;
    private String name;
    private List<Variant> variants;
    
    // Business methods
    public void addVariant(Variant variant) {
        // Business logic
    }
}
```

**Persistence Model (JPA Entity):**
```java
// infrastructure/persistence/ProductEntity.java
@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    private UUID id;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<VariantEntity> variants;
    
    // Mapping methods
    public Product toDomain() { ... }
    public static ProductEntity fromDomain(Product product) { ... }
}
```

---

## Guidelines & Enforcement

### Team Discipline Rules:

1. **Package Dependencies**
   ```
   infrastructure → application → domain
   ✅ infrastructure can import domain
   ❌ domain CANNOT import infrastructure
   ```

2. **Naming Conventions**
   - Domain models: `Product`, `Variant`, `Media`
   - Persistence models: `ProductEntity`, `VariantEntity`, `MediaEntity`
   - Ports: `*Port` suffix (e.g., `ProductRepositoryPort`)
   - Adapters: `*Adapter` suffix (e.g., `ProductRepositoryAdapter`)

3. **Code Review Checklist**
   - [ ] Domain classes have no framework imports
   - [ ] Infrastructure classes use `*Entity` suffix
   - [ ] Mappers exist between domain ↔ persistence
   - [ ] Use cases depend on ports, not concrete adapters

4. **ArchUnit Tests** (enforcement)
   ```java
   @ArchTest
   static final ArchRule domain_should_not_depend_on_infrastructure =
       noClasses()
           .that().resideInPackage("..domain..")
           .should().dependOnClassesThat()
           .resideInPackage("..infrastructure..");
   ```

---

## When to Use Which Approach?

### Use Multi-Module When:
- ✅ Service complexity: >50 classes
- ✅ Team size: >5 developers
- ✅ High reusability needs (domain extraction)
- ✅ Educational/showcase projects
- ✅ Strict compliance requirements

### Use Single-Module When:
- ✅ Service size: <50 classes (most microservices)
- ✅ Team ownership: 1-3 developers
- ✅ Velocity is priority
- ✅ Mature team with discipline
- ✅ Production pragmatism

---

## Consequences

### Positive:
- ✅ Audit Service serves as **reference architecture** for team learning
- ✅ Product/Files Services optimized for **development velocity**
- ✅ Balance between **academic purity** and **pragmatic delivery**
- ✅ Can migrate single-module → multi-module if service grows
- ✅ New team members learn from Audit, apply to Product/Files

### Negative:
- ⚠️ Mixed architecture styles require clear documentation
- ⚠️ Team needs discipline for single-module services
- ⚠️ Risk of architecture drift if not monitored

### Mitigations:
- 📝 Document clearly in README and onboarding
- 🧪 ArchUnit tests for package dependency rules
- 👁️ Code review focus on separation concerns
- 📊 Regular architecture health checks

---

## Migration Path

### From Multi-Module to Single-Module:
```bash
# 1. Create new package structure
mkdir -p src/main/java/.../product/{domain,application,infrastructure}

# 2. Move code
product-domain/src/main/java/**         → src/main/java/.../domain/
product-application/src/main/java/**    → src/main/java/.../application/
product-adapters-in-rest/**             → src/main/java/.../infrastructure/rest/
product-adapters-out-jpa/**             → src/main/java/.../infrastructure/persistence/
product-adapters-out-kafka/**           → src/main/java/.../infrastructure/messaging/

# 3. Merge POMs
# Combine all dependencies into single pom.xml

# 4. Separate domain from persistence
# Create *Entity classes for JPA
# Keep domain classes as POJOs
# Add mappers: domain ↔ entity

# 5. Add ArchUnit tests
# Enforce package dependency rules
```

---

## References

- [Hexagonal Architecture (Alistair Cockburn)](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture (Robert C. Martin)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Pragmatic Clean Architecture](https://www.jamesmichaelhickey.com/pragmatic-clean-architecture/)
- [Package by Feature vs Layer](https://phauer.com/2020/package-by-feature/)

---

## Review Schedule

**Next Review:** 2025-12-10 (1 month)  
**Trigger Events:**
- Service grows beyond 50 classes
- Team feedback on friction points
- Performance issues with build times
- New services being added
