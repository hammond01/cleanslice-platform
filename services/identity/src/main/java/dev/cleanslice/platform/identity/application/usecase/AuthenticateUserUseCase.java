package dev.cleanslice.platform.identity.application.usecase;

import dev.cleanslice.platform.identity.application.port.KeycloakAuthPort;
import org.springframework.stereotype.Service;

/**
 * Use case for user authentication via Keycloak.
 */
@Service
public class AuthenticateUserUseCase {

    private final KeycloakAuthPort keycloakAuthPort;

    public AuthenticateUserUseCase(KeycloakAuthPort keycloakAuthPort) {
        this.keycloakAuthPort = keycloakAuthPort;
    }

    public String authenticate(String username, String password) {
        return keycloakAuthPort.authenticate(username, password);
    }
}