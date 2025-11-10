package dev.cleanslice.platform.product.application.usecase;

import dev.cleanslice.platform.product.application.port.ProductEventPublisherPort;
import dev.cleanslice.platform.product.application.port.ProductRepositoryPort;
import dev.cleanslice.platform.product.domain.Product;
import dev.cleanslice.platform.product.domain.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Use case for creating a new product.
 * Demonstrates application layer orchestrating domain logic and infrastructure.
 */
@Service
@Transactional
public class CreateProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final ProductEventPublisherPort eventPublisher;

    public CreateProductUseCase(
            ProductRepositoryPort productRepository,
            ProductEventPublisherPort eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Execute the use case to create a product.
     *
     * @param ownerId the owner ID
     * @param name the product name
     * @param description the product description
     * @return the created product
     */
    public Product execute(UUID ownerId, String name, String description) {
        // Validate inputs
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }

        // Create product using domain model
        Product product = new Product(
                UUID.randomUUID(),
                ownerId,
                name,
                description,
                ProductStatus.DRAFT,
                new ArrayList<>(),
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );

        // Save to repository (infrastructure)
        Product savedProduct = productRepository.save(product);

        // Publish event (infrastructure)
        eventPublisher.publishProductCreated(savedProduct);

        return savedProduct;
    }
}
