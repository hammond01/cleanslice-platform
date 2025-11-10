package dev.cleanslice.platform.common.events;

import java.util.UUID;

public class ProductCreatedEvent extends DomainEvent {
    private final UUID productId;
    private final UUID ownerId;
    private final String name;

    public ProductCreatedEvent(UUID productId, UUID ownerId, String name) {
        super("ProductCreatedEvent");
        this.productId = productId;
        this.ownerId = ownerId;
        this.name = name;
    }

    // Getters
    public UUID getProductId() { return productId; }
    public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
}