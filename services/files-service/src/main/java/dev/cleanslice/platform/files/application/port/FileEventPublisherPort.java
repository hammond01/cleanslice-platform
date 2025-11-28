package dev.cleanslice.platform.files.application.port;

import dev.cleanslice.platform.files.domain.FileEntry;
import dev.cleanslice.platform.files.domain.FileVersion;

import java.util.UUID;

/**
 * Output port for publishing file events to Kafka.
 */
public interface FileEventPublisherPort {
    void publishFileUploaded(FileEntry fileEntry);
    void publishFileVersionUploaded(FileVersion fileVersion);
    void publishFileDeleted(UUID fileId);
}
