package dev.cleanslice.platform.product.infrastructure.persistence.adapter;

import dev.cleanslice.platform.product.application.port.ProductRepositoryPort;
import dev.cleanslice.platform.product.domain.Product;
import dev.cleanslice.platform.product.domain.ProductStatus;
import dev.cleanslice.platform.product.infrastructure.persistence.entity.ProductEntity;
import dev.cleanslice.platform.product.infrastructure.persistence.mapper.ProductMapper;
import dev.cleanslice.platform.product.infrastructure.persistence.repository.JpaProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/// Adapter that implements ProductRepositoryPort using JPA.
/// This is the infrastructure implementation of the port defined in application layer.
/// Key pattern: Domain depends on Port (interface), this adapter implements the Port.
/// Domain never knows about JPA - that's hexagonal architecture in action!
@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final JpaProductRepository jpaRepository;
    private final ProductMapper mapper;

    public ProductRepositoryAdapter(JpaProductRepository jpaRepository, ProductMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        ProductEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Product> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findPublishedProducts() {
        return jpaRepository.findByStatus(ProductStatus.PUBLISHED).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
