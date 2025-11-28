package dev.cleanslice.platform.files.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a file version.
 * Each file can have multiple versions for history tracking.
 */
public class FileVersion {
    private final UUID id;
    private final UUID fileId;
    private final int versionNumber;
    private final String name;
    private final String contentType;
    private final long size;
    private final String storageKey; // Key in MinIO/S3
    private final Instant createdAt;
    private final UUID createdBy;

    // Full constructor for reconstruction
    public FileVersion(UUID id, UUID fileId, int versionNumber, String name,
                      String contentType, long size, String storageKey,
                      Instant createdAt, UUID createdBy) {
        this.id = id;
        this.fileId = fileId;
        this.versionNumber = versionNumber;
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.storageKey = storageKey;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    // Factory method for new version
    public static FileVersion create(UUID fileId, int versionNumber, String name,
                                   String contentType, long size, String storageKey, UUID createdBy) {
        return new FileVersion(UUID.randomUUID(), fileId, versionNumber, name,
                             contentType, size, storageKey, Instant.now(), createdBy);
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getFileId() { return fileId; }
    public int getVersionNumber() { return versionNumber; }
    public String getName() { return name; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public String getStorageKey() { return storageKey; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getCreatedBy() { return createdBy; }
}