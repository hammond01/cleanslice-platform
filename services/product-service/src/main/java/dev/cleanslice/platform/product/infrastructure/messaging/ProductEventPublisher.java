package dev.cleanslice.platform.product.infrastructure.messaging;

import dev.cleanslice.platform.product.application.port.ProductEventPublisherPort;
import dev.cleanslice.platform.product.domain.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka adapter for publishing product events.
 * Implements the port defined in application layer.
 */
@Component
public class ProductEventPublisher implements ProductEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);
    private static final String TOPIC_PRODUCT_EVENTS = "product-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishProductCreated(Product product) {
        Map<String, Object> event = createBaseEvent("ProductCreated", product);
        publishEvent(event, product.getId().toString());
    }

    @Override
    public void publishProductUpdated(Product product) {
        Map<String, Object> event = createBaseEvent("ProductUpdated", product);
        publishEvent(event, product.getId().toString());
    }

    @Override
    public void publishProductPublished(Product product) {
        Map<String, Object> event = createBaseEvent("ProductPublished", product);
        publishEvent(event, product.getId().toString());
    }

    @Override
    public void publishProductDeleted(String productId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ProductDeleted");
        event.put("productId", productId);
        event.put("timestamp", System.currentTimeMillis());
        publishEvent(event, productId);
    }

    private Map<String, Object> createBaseEvent(String eventType, Product product) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("productId", product.getId().toString());
        event.put("ownerId", product.getOwnerId().toString());
        event.put("name", product.getName());
        event.put("status", product.getStatus().toString());
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }

    private void publishEvent(Map<String, Object> event, String key) {
        try {
            kafkaTemplate.send(TOPIC_PRODUCT_EVENTS, key, event);
            log.info("Published event: {} for product: {}", event.get("eventType"), key);
        } catch (Exception e) {
            log.error("Failed to publish event: {} for product: {}", event.get("eventType"), key, e);
            // In production, you might want to use retry mechanism or dead letter queue
        }
    }
}
