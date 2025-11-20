package dev.cleanslice.platform.common.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Event emitted when a file is deleted.
 */
public class FileDeletedEvent extends DomainEvent {
    private final UUID fileId;
    private final String reason;

    protected FileDeletedEvent() {
        super("FileDeletedEvent");
        this.fileId = null;
        this.reason = null;
    }

    @JsonCreator
    public FileDeletedEvent(@JsonProperty("fileId") UUID fileId,
                            @JsonProperty("reason") String reason) {
        super("FileDeletedEvent");
        this.fileId = fileId;
        this.reason = reason;
    }

    public UUID getFileId() { return fileId; }
    public String getReason() { return reason; }
}
