# ADR 003: Modular Monolith to Microservices Migration Strategy

## Status
Accepted

## Context
The platform supports two deployment modes: modular monolith for local development and microservices for production.

## Decision
Implement both modes from the same codebase using Maven profiles and Spring profiles.

## Implementation
- **Modular Monolith Profile**: Single application with all services
- **Microservices Profile**: Separate applications communicating via REST + Kafka
- Shared domain and application layers
- Profile-specific adapters and configurations

## Benefits
- **Development**: Faster local iteration with monolith
- **Production**: Independent scaling and deployment with microservices
- **Testing**: Easier integration testing in monolith mode
- **Migration**: Gradual transition path

## Consequences
- **Positive**: Flexibility in deployment strategies
- **Positive**: Shared business logic reduces duplication
- **Negative**: Complexity in build and configuration
- **Mitigation**: Clear separation of concerns and profile management