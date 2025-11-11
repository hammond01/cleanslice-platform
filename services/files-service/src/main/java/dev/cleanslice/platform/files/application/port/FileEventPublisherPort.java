package dev.cleanslice.platform.files.application.port;

import dev.cleanslice.platform.files.domain.FileEntry;

import java.util.UUID;

/**
 * Output port for publishing file events to Kafka.
 */
public interface FileEventPublisherPort {
    void publishFileUploaded(FileEntry fileEntry);
    void publishFileDeleted(UUID fileId);
}
