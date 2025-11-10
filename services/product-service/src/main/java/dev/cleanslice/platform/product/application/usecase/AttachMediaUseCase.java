package dev.cleanslice.platform.product.application.usecase;

import dev.cleanslice.platform.product.application.port.ProductEventPublisherPort;
import dev.cleanslice.platform.product.application.port.ProductRepositoryPort;
import dev.cleanslice.platform.product.domain.Media;
import dev.cleanslice.platform.product.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for attaching media to a product.
 * Demonstrates cross-service coordination (Product + Files services).
 */
@Service
@Transactional
public class AttachMediaUseCase {

    private final ProductRepositoryPort productRepository;
    private final ProductEventPublisherPort eventPublisher;

    public AttachMediaUseCase(
            ProductRepositoryPort productRepository,
            ProductEventPublisherPort eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Attach media to product.
     *
     * @param productId the product ID
     * @param fileId the file ID from Files service
     * @param altText alternative text for the media
     * @param isPrimary whether this is the primary media
     * @return the updated product
     */
    public Product execute(UUID productId, UUID fileId, String altText, boolean isPrimary) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Create media using domain model
        Media media = Media.create(fileId, altText, product.getMediaList().size(), isPrimary);

        // Add media using domain method
        product.addMedia(media);

        // Save
        Product savedProduct = productRepository.save(product);

        // Publish event
        eventPublisher.publishProductUpdated(savedProduct);

        return savedProduct;
    }
}
