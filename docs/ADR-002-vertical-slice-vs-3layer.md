# ADR 002: Vertical Slice Architecture vs 3-Layer Architecture

## Status
Accepted

## Context
We need to decide between organizing code by technical layers (presentation, business, data) or by business features (vertical slices).

## Decision
Use Vertical Slice Architecture within each service, combined with Hexagonal Architecture principles.

## Rationale
- Better alignment with business domains
- Easier to understand and maintain feature boundaries
- Supports independent deployment and scaling
- Clear separation of concerns with ports & adapters

## Implementation
Each service follows:
- Domain layer: Entities, business rules, ports
- Application layer: Use cases, handlers
- Adapters: REST controllers, JPA repositories, external integrations

## Consequences
- **Positive**: Feature-focused development
- **Positive**: Clear boundaries between business capabilities
- **Negative**: Some code duplication across features
- **Mitigation**: Shared common libraries for cross-cutting concerns