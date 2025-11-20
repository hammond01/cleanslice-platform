package dev.cleanslice.platform.common.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class ProductCreatedEvent extends DomainEvent {
    private final UUID productId;
    private final UUID ownerId;
    private final String name;

    // Default constructor for Jackson
    public ProductCreatedEvent() {
        super("ProductCreatedEvent");
        this.productId = null;
        this.ownerId = null;
        this.name = null;
    }

    @JsonCreator
    public ProductCreatedEvent(
            @JsonProperty("productId") UUID productId, 
            @JsonProperty("ownerId") UUID ownerId, 
            @JsonProperty("name") String name) {
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