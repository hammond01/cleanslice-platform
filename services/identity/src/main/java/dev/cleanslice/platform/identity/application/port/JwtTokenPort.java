package dev.cleanslice.platform.identity.application.port;

import dev.cleanslice.platform.identity.domain.User;

/**
 * Port for JWT token operations.
 */
public interface JwtTokenPort {
    String generateToken(User user);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
    User getUserFromToken(String token);
}