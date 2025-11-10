package dev.cleanslice.platform.common.events;

import java.util.UUID;

public class FileUploadedEvent extends DomainEvent {
    private final UUID fileId;
    private final UUID ownerId;
    private final UUID versionId;
    private final long size;
    private final String contentType;

    public FileUploadedEvent(UUID fileId, UUID ownerId, UUID versionId, long size, String contentType) {
        super("FileUploadedEvent");
        this.fileId = fileId;
        this.ownerId = ownerId;
        this.versionId = versionId;
        this.size = size;
        this.contentType = contentType;
    }

    // Getters
    public UUID getFileId() { return fileId; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getVersionId() { return versionId; }
    public long getSize() { return size; }
    public String getContentType() { return contentType; }
}