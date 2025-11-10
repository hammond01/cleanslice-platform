package dev.cleanslice.platform.audit.adapters.in.kafka;

import dev.cleanslice.platform.audit.domain.AuditLog;
import dev.cleanslice.platform.audit.adapters.out.jpa.AuditLogRepository;
import dev.cleanslice.platform.common.events.FileUploadedEvent;
import dev.cleanslice.platform.common.events.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumer.class);
    private final AuditLogRepository auditLogRepository;

    public AuditEventConsumer(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @KafkaListener(topics = "files.events.v1", groupId = "audit-service")
    public void consumeFileEvent(FileUploadedEvent event) {
        log.info("Received file event: FileUploadedEvent");
        
        var auditLog = new AuditLog(
                "FileUploadedEvent",
                event.getFileId(),
                "FILE",
                event.getOwnerId(),
                "FILE_UPLOADED",
                String.format("File uploaded: size=%d, contentType=%s", event.getSize(), event.getContentType()),
                event.getOccurredAt()
        );
        
        auditLogRepository.save(auditLog);
        log.info("Audit log saved for file: {}", event.getFileId());
    }

    @KafkaListener(topics = "products.events.v1", groupId = "audit-service")
    public void consumeProductEvent(ProductCreatedEvent event) {
        log.info("Received product event: ProductCreatedEvent");
        
        var auditLog = new AuditLog(
                "ProductCreatedEvent",
                event.getProductId(),
                "PRODUCT",
                event.getOwnerId(),
                "PRODUCT_CREATED",
                String.format("Product created: name=%s", event.getName()),
                event.getOccurredAt()
        );
        
        auditLogRepository.save(auditLog);
        log.info("Audit log saved for product: {}", event.getProductId());
    }
}
