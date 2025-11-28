package dev.cleanslice.platform.files.infrastructure.messaging;

import dev.cleanslice.platform.files.application.port.FileEventPublisherPort;
import dev.cleanslice.platform.files.domain.FileEntry;
import dev.cleanslice.platform.files.domain.FileVersion;
import dev.cleanslice.platform.common.events.FileUploadedEvent;
import dev.cleanslice.platform.common.events.FileVersionUploadedEvent;
import dev.cleanslice.platform.common.events.FileDeletedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Kafka implementation of FileEventPublisherPort.
 */
@Component
public class FileEventPublisher implements FileEventPublisherPort {

    private static final String TOPIC = "files.events.v1";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FileEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishFileUploaded(FileEntry fileEntry) {
        var event = new FileUploadedEvent(
            fileEntry.getId(),
            fileEntry.getOwnerId(),
            null,
            fileEntry.getSize(),
            fileEntry.getContentType()
        );

        ProducerRecord<String, Object> rec = new ProducerRecord<>(TOPIC, fileEntry.getId().toString(), event);
        rec.headers().add("__TypeId__", FileUploadedEvent.class.getName().getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
    }

    @Override
    public void publishFileVersionUploaded(FileVersion fileVersion) {
        var event = new FileVersionUploadedEvent(
            fileVersion.getFileId(),
            fileVersion.getId(),
            fileVersion.getVersionNumber(),
            fileVersion.getCreatedBy(),
            fileVersion.getName(),
            fileVersion.getContentType(),
            fileVersion.getSize()
        );

        ProducerRecord<String, Object> rec = new ProducerRecord<>(TOPIC, fileVersion.getFileId().toString(), event);
        rec.headers().add("__TypeId__", FileVersionUploadedEvent.class.getName().getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
    }

    @Override
    public void publishFileDeleted(UUID fileId) {
        var event = new FileDeletedEvent(fileId, "deleted-by-user");
        ProducerRecord<String, Object> rec = new ProducerRecord<>(TOPIC, fileId.toString(), event);
        rec.headers().add("__TypeId__", FileDeletedEvent.class.getName().getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(rec);
    }
}
