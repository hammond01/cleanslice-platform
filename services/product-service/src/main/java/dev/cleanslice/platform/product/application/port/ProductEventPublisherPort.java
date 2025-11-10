package dev.cleanslice.platform.product.application.port;

import dev.cleanslice.platform.product.domain.Product;

/**
 * Event publisher port for Product events.
 * This is the interface that defines what the domain needs to publish events.
 * Implementation will be in infrastructure layer (Kafka adapter).
 */
public interface ProductEventPublisherPort {

    /**
     * Publish event when a product is created.
     *
     * @param product the created product
     */
    void publishProductCreated(Product product);

    /**
     * Publish event when a product is updated.
     *
     * @param product the updated product
     */
    void publishProductUpdated(Product product);

    /**
     * Publish event when a product is published (status changed to PUBLISHED).
     *
     * @param product the published product
     */
    void publishProductPublished(Product product);

    /**
     * Publish event when a product is deleted.
     *
     * @param productId the deleted product ID
     */
    void publishProductDeleted(String productId);
}
