package dev.cleanslice.platform.product.infrastructure.messaging;

import dev.cleanslice.platform.product.application.port.ProductEventPublisherPort;
import dev.cleanslice.platform.product.domain.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of ProductEventPublisherPort for dev-local profile.
 * Simply logs events without actual Kafka publishing to avoid timeouts.
 */
@Component
@Profile("dev-local")
public class MockProductEventPublisher implements ProductEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(MockProductEventPublisher.class);

    @Override
    public void publishProductCreated(Product product) {
        log.info("[MOCK] ProductCreated event: productId={}, name={}, ownerId={}", 
                product.getId(), product.getName(), product.getOwnerId());
    }

    @Override
    public void publishProductUpdated(Product product) {
        log.info("[MOCK] ProductUpdated event: productId={}, name={}, status={}", 
                product.getId(), product.getName(), product.getStatus());
    }

    @Override
    public void publishProductPublished(Product product) {
        log.info("[MOCK] ProductPublished event: productId={}, name={}", 
                product.getId(), product.getName());
    }

    @Override
    public void publishProductDeleted(String productId) {
        log.info("[MOCK] ProductDeleted event: productId={}", productId);
    }
}
