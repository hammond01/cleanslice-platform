package dev.cleanslice.platform.identity.application.port;

import dev.cleanslice.platform.identity.domain.User;

/**
 * Port for password encoding operations.
 */
public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}