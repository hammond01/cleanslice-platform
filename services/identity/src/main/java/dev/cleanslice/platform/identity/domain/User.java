package dev.cleanslice.platform.identity.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a user.
 * Pure POJO without JPA annotations - follows clean architecture principles.
 */
public class User {
    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final boolean enabled;

    // Full constructor for reconstruction
    public User(UUID id, String username, String email, String passwordHash,
               Instant createdAt, Instant updatedAt, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.enabled = enabled;
    }

    // Factory method for new users
    public static User create(String username, String email, String passwordHash) {
        var now = Instant.now();
        return new User(UUID.randomUUID(), username, email, passwordHash, now, now, true);
    }

    // Business methods
    public User changePassword(String newPasswordHash) {
        return new User(id, username, email, newPasswordHash, createdAt, Instant.now(), enabled);
    }

    public User disable() {
        return new User(id, username, email, passwordHash, createdAt, Instant.now(), false);
    }

    public User enable() {
        return new User(id, username, email, passwordHash, createdAt, Instant.now(), true);
    }

    // Getters
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public boolean isEnabled() { return enabled; }
}