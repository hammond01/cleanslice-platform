package dev.cleanslice.platform.identity.application.usecase;

import dev.cleanslice.platform.identity.application.port.KeycloakAuthPort;
import dev.cleanslice.platform.identity.application.port.PasswordEncoderPort;
import dev.cleanslice.platform.identity.application.port.UserRepositoryPort;
import dev.cleanslice.platform.identity.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for user registration.
 * Supports both local and Keycloak registration.
 */
@Service
@Transactional
public class RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final KeycloakAuthPort keycloakAuthPort;
    private final boolean useKeycloak;

    public RegisterUserUseCase(UserRepositoryPort userRepositoryPort,
                              PasswordEncoderPort passwordEncoderPort,
                              KeycloakAuthPort keycloakAuthPort,
                              @Value("${auth.provider:jwt}") String authProvider) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.keycloakAuthPort = keycloakAuthPort;
        this.useKeycloak = "keycloak".equals(authProvider);
    }

    public User register(String username, String email, String password) {
        if (useKeycloak) {
            return keycloakAuthPort.register(username, email, password);
        } else {
            // Local registration
            if (userRepositoryPort.existsByUsername(username)) {
                throw new RuntimeException("Username already exists");
            }

            if (userRepositoryPort.existsByEmail(email)) {
                throw new RuntimeException("Email already exists");
            }

            String passwordHash = passwordEncoderPort.encode(password);
            User user = User.create(username, email, passwordHash);

            return userRepositoryPort.save(user);
        }
    }
}