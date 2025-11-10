package dev.cleanslice.platform.product.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Variant domain model - Pure POJO
 */
public class Variant {

    private UUID id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private Instant createdAt;
    private Instant updatedAt;

    // Default constructor
    public Variant() {
        this.id = UUID.randomUUID();
        this.stock = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Business constructor
    public Variant(String name, String sku, BigDecimal price, Integer stock) {
        this();
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stock = stock;
    }

    // Full constructor
    public Variant(UUID id, String name, String sku, BigDecimal price, Integer stock,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stock = stock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Business methods
    public void updatePrice(BigDecimal newPrice) {
        this.price = newPrice;
        this.updatedAt = Instant.now();
    }

    public void adjustStock(Integer adjustment) {
        this.stock += adjustment;
        if (this.stock < 0) {
            this.stock = 0;
        }
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    void setId(UUID id) {
        this.id = id;
    }
}
