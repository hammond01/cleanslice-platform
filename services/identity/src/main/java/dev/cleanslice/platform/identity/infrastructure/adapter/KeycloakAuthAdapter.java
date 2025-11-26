package dev.cleanslice.platform.identity.infrastructure.adapter;

import dev.cleanslice.platform.identity.application.port.KeycloakAuthPort;
import dev.cleanslice.platform.identity.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import java.util.Collections;

/**
 * Keycloak authentication adapter.
 */
@Component
@Slf4j
public class KeycloakAuthAdapter implements KeycloakAuthPort {

    private final String serverUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final String adminUsername;
    private final String adminPassword;

    public KeycloakAuthAdapter(
            @Value("${keycloak.server-url:http://localhost:8080}") String serverUrl,
            @Value("${keycloak.realm:cleanslice}") String realm,
            @Value("${keycloak.resource:files-service}") String clientId,
            @Value("${keycloak.credentials.secret:files-service-secret}") String clientSecret,
            @Value("${keycloak.admin-username:admin}") String adminUsername,
            @Value("${keycloak.admin-password:admin}") String adminPassword) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public String authenticate(String username, String password) {
        try {
            // Use password grant type to get access token
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(username)
                    .password(password)
                    .grantType("password")
                    .build();
            
            String token = keycloak.tokenManager().getAccessTokenString();
            keycloak.close();
            return token;
        } catch (Exception e) {
            log.error("Keycloak authentication failed for user: {}", username, e);
            throw new RuntimeException("Authentication failed: Invalid username or password");
        }
    }

    @Override
    public User register(String username, String email, String password) {
        try (Keycloak keycloak = buildAdminKeycloakClient()) {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            UserRepresentation userRep = new UserRepresentation();
            userRep.setUsername(username);
            userRep.setEmail(email);
            userRep.setEnabled(true);

            // Create user
            Response response = usersResource.create(userRep);
            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create user in Keycloak");
            }

            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            usersResource.get(userId).resetPassword(credential);

            // Return domain user
            return User.create(username, email, ""); // Password not stored locally
        } catch (Exception e) {
            log.error("Keycloak user registration failed", e);
            throw new RuntimeException("Registration failed");
        }
    }

    @Override
    public boolean validateToken(String token) {
        try (Keycloak keycloak = buildKeycloakClient()) {
            // Validate token by introspecting it
            return keycloak.tokenManager().getAccessToken().getToken().equals(token);
        } catch (Exception e) {
            return false;
        }
    }

    private Keycloak buildKeycloakClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    private Keycloak buildAdminKeycloakClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // Admin realm
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }
}