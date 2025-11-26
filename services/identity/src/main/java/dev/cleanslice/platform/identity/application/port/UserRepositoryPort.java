package dev.cleanslice.platform.identity.application.port;

import dev.cleanslice.platform.identity.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Port for user repository operations.
 */
public interface UserRepositoryPort {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}