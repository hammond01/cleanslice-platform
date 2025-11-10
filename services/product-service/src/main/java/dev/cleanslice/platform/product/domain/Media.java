package dev.cleanslice.platform.product.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Media domain model - Pure POJO
 * References a file from Files Service
 */
public class Media {

    private UUID id;
    private UUID fileId; // Reference to Files service
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary;
    private Instant createdAt;

    // Default constructor
    public Media() {
        this.id = UUID.randomUUID();
        this.sortOrder = 0;
        this.isPrimary = false;
        this.createdAt = Instant.now();
    }

    // Business constructor
    public Media(UUID fileId, String altText, Integer sortOrder, Boolean isPrimary) {
        this();
        this.fileId = fileId;
        this.altText = altText;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    // Full constructor
    public Media(UUID id, UUID fileId, String altText, Integer sortOrder, 
                Boolean isPrimary, Instant createdAt) {
        this.id = id;
        this.fileId = fileId;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.isPrimary = isPrimary;
        this.createdAt = createdAt;
    }

    // Static factory method
    public static Media create(UUID fileId, String altText, Integer sortOrder, Boolean isPrimary) {
        return new Media(fileId, altText, sortOrder, isPrimary);
    }

    // Business methods
    public void markAsPrimary() {
        this.isPrimary = true;
    }

    public void unmarkAsPrimary() {
        this.isPrimary = false;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getFileId() {
        return fileId;
    }

    public String getAltText() {
        return altText;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    // Boolean accessor method (standard naming)
    public boolean isPrimary() {
        return Boolean.TRUE.equals(isPrimary);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setAltText(String altText) {
        this.altText = altText;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    void setId(UUID id) {
        this.id = id;
    }
}
