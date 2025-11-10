package dev.cleanslice.platform.product.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Media domain model - Pure POJO
 * References a file from Files Service
 */
@Getter
public class Media {

    private final UUID id;
    private final UUID fileId; // Reference to Files service - immutable after creation
    @Setter
    private String altText;
    @Setter
    private Integer sortOrder;
    @Setter
    private Boolean isPrimary;
    private final Instant createdAt;

    // Business constructor
    public Media(UUID fileId, String altText, Integer sortOrder, Boolean isPrimary) {
        this.id = UUID.randomUUID();
        this.fileId = fileId;
        this.altText = altText;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.createdAt = Instant.now();
    }

    // Full constructor for mapper
    public Media(UUID id, UUID fileId, String altText, Integer sortOrder, 
                Boolean isPrimary, Instant createdAt) {
        this.id = id;
        this.fileId = fileId;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.isPrimary = isPrimary;
        this.createdAt = createdAt;
    }

    public static Media create(UUID fileId, String altText, Integer sortOrder, Boolean isPrimary) {
        return new Media(fileId, altText, sortOrder, isPrimary);
    }
    public boolean isPrimary() {
        return Boolean.TRUE.equals(isPrimary);
    }

}
