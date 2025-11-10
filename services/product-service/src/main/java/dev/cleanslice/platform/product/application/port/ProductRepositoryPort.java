package dev.cleanslice.platform.product.application.port;

import dev.cleanslice.platform.product.domain.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Product aggregate.
 * This is the interface that defines what the domain needs from persistence.
 * Implementation will be in infrastructure layer (ProductRepositoryAdapter).
 */
public interface ProductRepositoryPort {

    /**
     * Save a product (create or update).
     *
     * @param product the product to save
     * @return the saved product
     */
    Product save(Product product);

    /**
     * Find product by ID.
     *
     * @param id the product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(UUID id);

    /**
     * Find all products by owner ID.
     *
     * @param ownerId the owner ID
     * @return list of products
     */
    List<Product> findByOwnerId(UUID ownerId);

    /**
     * Find all published products.
     *
     * @return list of published products
     */
    List<Product> findPublishedProducts();

    /**
     * Delete a product by ID.
     *
     * @param id the product ID
     */
    void deleteById(UUID id);

    /**
     * Check if product exists by ID.
     *
     * @param id the product ID
     * @return true if exists
     */
    boolean existsById(UUID id);
}
