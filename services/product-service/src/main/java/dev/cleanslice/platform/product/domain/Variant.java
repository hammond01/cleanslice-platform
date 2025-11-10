package dev.cleanslice.platform.product.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Variant {

    private final UUID id;
    private String name;
    @Setter
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private final Instant createdAt;
    private Instant updatedAt;

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
    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        this.price = newPrice;
        this.updatedAt = Instant.now();
    }

    public void adjustStock(Integer adjustment) {
        if (adjustment == null) {
            throw new IllegalArgumentException("Stock adjustment cannot be null");
        }
        this.stock += adjustment;
        if (this.stock < 0) {
            this.stock = 0;
        }
        this.updatedAt = Instant.now();
    }
}
