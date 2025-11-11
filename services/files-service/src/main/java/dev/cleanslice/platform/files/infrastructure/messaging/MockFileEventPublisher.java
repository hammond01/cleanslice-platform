package dev.cleanslice.platform.files.infrastructure.messaging;

import dev.cleanslice.platform.files.application.port.FileEventPublisherPort;
import dev.cleanslice.platform.files.domain.FileEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock implementation for dev-local profile.
 * Prevents Kafka connection timeouts during development.
 */
@Component
@Profile("dev-local")
@Slf4j
public class MockFileEventPublisher implements FileEventPublisherPort {

    @Override
    public void publishFileUploaded(FileEntry fileEntry) {
        log.info("[MOCK] FILE_UPLOADED event: fileId={}, filename={}, size={}",
                fileEntry.getId(), fileEntry.getName(), fileEntry.getSize());
    }

    @Override
    public void publishFileDeleted(UUID fileId) {
        log.info("[MOCK] FILE_DELETED event: fileId={}", fileId);
    }
}
