package dev.cleanslice.platform.common.events;

import java.util.UUID;

/**
 * Event published when a new file version is uploaded.
 */
public class FileVersionUploadedEvent extends DomainEvent {

    private final UUID fileId;
    private final UUID versionId;
    private final int versionNumber;
    private final UUID ownerId;
    private final String filename;
    private final String contentType;
    private final long size;

    public FileVersionUploadedEvent(UUID fileId, UUID versionId, int versionNumber,
                                   UUID ownerId, String filename, String contentType, long size) {
        super("FileVersionUploaded");
        this.fileId = fileId;
        this.versionId = versionId;
        this.versionNumber = versionNumber;
        this.ownerId = ownerId;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }

    // Getters
    public UUID getFileId() { return fileId; }
    public UUID getVersionId() { return versionId; }
    public int getVersionNumber() { return versionNumber; }
    public UUID getOwnerId() { return ownerId; }
    public String getFilename() { return filename; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
}