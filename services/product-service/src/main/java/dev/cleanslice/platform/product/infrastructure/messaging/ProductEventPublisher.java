package dev.cleanslice.platform.product.infrastructure.messaging;

import dev.cleanslice.platform.common.events.ProductCreatedEvent;
import dev.cleanslice.platform.product.application.port.ProductEventPublisherPort;
import dev.cleanslice.platform.product.domain.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.nio.charset.StandardCharsets;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter for publishing product events.
 * Implements the port defined in application layer.
 */
@Component
public class ProductEventPublisher implements ProductEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);
    private static final String TOPIC_PRODUCT_EVENTS = "products.events.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishProductCreated(Product product) {
        ProductCreatedEvent event = new ProductCreatedEvent(
            product.getId(),
            product.getOwnerId(),
            product.getName()
        );
        publishEvent(event, product.getId().toString(), "ProductCreated");
    }

    @Override
    public void publishProductUpdated(Product product) {
        ProductCreatedEvent event = new ProductCreatedEvent(
            product.getId(),
            product.getOwnerId(),
            product.getName()
        );
        publishEvent(event, product.getId().toString(), "ProductUpdated");
    }

    @Override
    public void publishProductPublished(Product product) {
        ProductCreatedEvent event = new ProductCreatedEvent(
            product.getId(),
            product.getOwnerId(),
            product.getName()
        );
        publishEvent(event, product.getId().toString(), "ProductPublished");
    }

    @Override
    public void publishProductDeleted(String productId) {
        // For deleted products, we create a minimal event
        // In production, you might want a separate ProductDeletedEvent
        ProductCreatedEvent event = new ProductCreatedEvent(
            java.util.UUID.fromString(productId),
            java.util.UUID.randomUUID(), // system user
            "DELETED"
        );
        publishEvent(event, productId, "ProductDeleted");
    }

    @SuppressWarnings("null")
    private void publishEvent(Object event, String key, String eventType) {
        try {
            ProducerRecord<String, Object> rec = new ProducerRecord<>(TOPIC_PRODUCT_EVENTS, key, event);
            rec.headers().add("__TypeId__", event.getClass().getName().getBytes(StandardCharsets.UTF_8));
            kafkaTemplate.send(rec);
            log.info("Published event: {} for product: {}", eventType, key);
        } catch (Exception e) {
            log.error("Failed to publish event: {} for product: {}", eventType, key, e);
            // In production, you might want to use retry mechanism or dead letter queue
        }
    }
}
