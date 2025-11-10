package dev.cleanslice.platform.product.infrastructure.persistence.entity;

import dev.cleanslice.platform.product.domain.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity for Product.
 * This is the PERSISTENCE MODEL - contains JPA annotations.
 * Separate from domain model to keep domain clean.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<VariantEntity> variants = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<MediaEntity> mediaList = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Helper methods for bidirectional relationships
    public void addVariant(VariantEntity variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(VariantEntity variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    public void addMedia(MediaEntity media) {
        mediaList.add(media);
        media.setProduct(this);
    }

    public void removeMedia(MediaEntity media) {
        mediaList.remove(media);
        media.setProduct(null);
    }
}
