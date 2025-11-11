package dev.cleanslice.platform.files.infrastructure.messaging;

import dev.cleanslice.platform.files.application.port.FileEventPublisherPort;
import dev.cleanslice.platform.files.domain.FileEntry;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka implementation of FileEventPublisherPort.
 * Only active when NOT in dev-local profile.
 */
@Component
@Profile("!dev-local")
public class FileEventPublisher implements FileEventPublisherPort {

    private static final String TOPIC = "file-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FileEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishFileUploaded(FileEntry fileEntry) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "FILE_UPLOADED");
        event.put("fileId", fileEntry.getId().toString());
        event.put("ownerId", fileEntry.getOwnerId().toString());
        event.put("filename", fileEntry.getName());
        event.put("size", fileEntry.getSize());
        event.put("contentType", fileEntry.getContentType());
        event.put("timestamp", fileEntry.getCreatedAt().toString());

        kafkaTemplate.send(TOPIC, fileEntry.getId().toString(), event);
    }

    @Override
    public void publishFileDeleted(UUID fileId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "FILE_DELETED");
        event.put("fileId", fileId.toString());
        event.put("timestamp", java.time.Instant.now().toString());

        kafkaTemplate.send(TOPIC, fileId.toString(), event);
    }
}
