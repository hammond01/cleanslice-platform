package dev.cleanslice.platform.product.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Product {

    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private ProductStatus status;
    private final List<Variant> variants;
    private final List<Media> mediaList;
    private final Instant createdAt;
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

    public void publish() {
        if (this.status == ProductStatus.PUBLISHED) {
            throw new IllegalStateException("Product is already published");
        }
        this.status = ProductStatus.PUBLISHED;
        this.updatedAt = Instant.now();
    }

    public void addMedia(Media media) {
        if (media == null) {
            throw new IllegalArgumentException("Media cannot be null");
        }
        this.mediaList.add(media);
        this.updatedAt = Instant.now();
    }

    public void addVariant(Variant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }
        this.variants.add(variant);
        this.updatedAt = Instant.now();
    }

    // Selective setters for mutable business fields
    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    // Defensive copy getters
    public List<Variant> getVariants() {
        return new ArrayList<>(variants);
    }

    public List<Media> getMediaList() {
        return new ArrayList<>(mediaList); // Defensive copy
    }

}
