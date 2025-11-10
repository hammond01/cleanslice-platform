package dev.cleanslice.platform.files.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public class FileEntry {
    private final UUID id;
    private final UUID ownerId;

    @NotNull
    @Size(max = 255)
    private final String name;

    @NotNull
    @Size(max = 100)
    private final String contentType;

    private final long size;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final boolean deleted;

    public FileEntry(UUID id, UUID ownerId, String name, String contentType, long size,
                    Instant createdAt, Instant updatedAt, boolean deleted) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    // Factory method for new files
    public static FileEntry create(UUID ownerId, String name, String contentType, long size) {
        var now = Instant.now();
        return new FileEntry(UUID.randomUUID(), ownerId, name, contentType, size, now, now, false);
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public boolean isDeleted() { return deleted; }

    // Business methods
    public FileEntry markDeleted() {
        return new FileEntry(id, ownerId, name, contentType, size, createdAt, Instant.now(), true);
    }

    public FileEntry restore() {
        return new FileEntry(id, ownerId, name, contentType, size, createdAt, Instant.now(), false);
    }
}