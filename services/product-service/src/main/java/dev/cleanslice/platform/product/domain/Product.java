package dev.cleanslice.platform.product.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Product domain model - Pure POJO without infrastructure dependencies
 * No JPA annotations here - this is the business domain model
 */
public class Product {

    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private ProductStatus status;
    private List<Variant> variants;
    private List<Media> mediaList;
    private Instant createdAt;
    private Instant updatedAt;

    // Default constructor
    public Product() {
        this.id = UUID.randomUUID();
        this.status = ProductStatus.DRAFT;
        this.variants = new ArrayList<>();
        this.mediaList = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Business constructor
    public Product(UUID ownerId, String name, String description) {
        this();
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
    }

    // Full constructor (for reconstruction from persistence)
    public Product(UUID id, UUID ownerId, String name, String description, 
                  ProductStatus status, List<Variant> variants, List<Media> mediaList,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.variants = variants != null ? new ArrayList<>(variants) : new ArrayList<>();
        this.mediaList = mediaList != null ? new ArrayList<>(mediaList) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Business methods
    public void publish() {
        if (this.status == ProductStatus.PUBLISHED) {
            throw new IllegalStateException("Product is already published");
        }
        this.status = ProductStatus.PUBLISHED;
        this.updatedAt = Instant.now();
    }

    public void addVariant(Variant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }
        this.variants.add(variant);
        this.updatedAt = Instant.now();
    }

    public void addMedia(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("Media cannot be null");
        }
        this.mediaList.add(media);
        this.updatedAt = Instant.now();
    }

    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public List<Variant> getVariants() {
        return new ArrayList<>(variants); // Defensive copy
    }

    public List<Media> getMediaList() {
        return new ArrayList<>(mediaList); // Defensive copy
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters (package-private for reconstruction)
    void setId(UUID id) {
        this.id = id;
    }

    void setStatus(ProductStatus status) {
        this.status = status;
    }

    void setVariants(List<Variant> variants) {
        this.variants = new ArrayList<>(variants);
    }

    void setMediaList(List<Media> mediaList) {
        this.mediaList = new ArrayList<>(mediaList);
    }
}
