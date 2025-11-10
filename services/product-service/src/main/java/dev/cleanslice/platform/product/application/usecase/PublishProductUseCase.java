package dev.cleanslice.platform.product.application.usecase;

import dev.cleanslice.platform.product.application.port.ProductEventPublisherPort;
import dev.cleanslice.platform.product.application.port.ProductRepositoryPort;
import dev.cleanslice.platform.product.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for publishing a product (changing status from DRAFT to PUBLISHED).
 */
@Service
@Transactional
public class PublishProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final ProductEventPublisherPort eventPublisher;

    public PublishProductUseCase(
            ProductRepositoryPort productRepository,
            ProductEventPublisherPort eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publish a product.
     *
     * @param productId the product ID
     * @return the published product
     * @throws IllegalArgumentException if product not found
     */
    public Product execute(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Use domain method to publish
        product.publish();

        // Save updated product
        Product savedProduct = productRepository.save(product);

        // Publish event
        eventPublisher.publishProductPublished(savedProduct);

        return savedProduct;
    }
}
