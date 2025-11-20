package dev.cleanslice.platform.common.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class FileUploadedEvent extends DomainEvent {
    private final UUID fileId;
    private final UUID ownerId;
    private final UUID versionId;
    private final long size;
    private final String contentType;

    // No-arg constructor for Jackson
    protected FileUploadedEvent() {
        super("FileUploadedEvent");
        this.fileId = null;
        this.ownerId = null;
        this.versionId = null;
        this.size = 0L;
        this.contentType = null;
    }

    @JsonCreator
    public FileUploadedEvent(
            @JsonProperty("fileId") UUID fileId,
            @JsonProperty("ownerId") UUID ownerId,
            @JsonProperty("versionId") UUID versionId,
            @JsonProperty("size") long size,
            @JsonProperty("contentType") String contentType) {
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