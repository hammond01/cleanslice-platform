package dev.cleanslice.platform.identity.application.port;

import dev.cleanslice.platform.identity.domain.User;

/**
 * Port for Keycloak authentication operations.
 */
public interface KeycloakAuthPort {
    String authenticate(String username, String password);
    User register(String username, String email, String password);
    boolean validateToken(String token);
}