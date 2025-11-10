package dev.cleanslice.platform.product.infrastructure.persistence.repository;

import dev.cleanslice.platform.product.domain.ProductStatus;
import dev.cleanslice.platform.product.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ProductEntity.
 * This is infrastructure - not visible to domain.
 */
@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

    /**
     * Find all products by owner ID.
     */
    List<ProductEntity> findByOwnerId(UUID ownerId);

    /**
     * Find all published products.
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.status = :status")
    List<ProductEntity> findByStatus(@Param("status") ProductStatus status);
}
