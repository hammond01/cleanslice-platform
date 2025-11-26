package dev.cleanslice.platform.identity.infrastructure.config;

import dev.cleanslice.platform.identity.application.port.JwtTokenPort;
import dev.cleanslice.platform.identity.application.port.PasswordEncoderPort;
import dev.cleanslice.platform.identity.infrastructure.adapter.JwtTokenAdapter;
import dev.cleanslice.platform.identity.infrastructure.adapter.PasswordEncoderAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application configuration for adapters.
 */
@Configuration
public class AppConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Bean
    public JwtTokenPort jwtTokenPort() {
        return new JwtTokenAdapter(jwtSecret, jwtExpiration);
    }

    @Bean
    public PasswordEncoderPort passwordEncoderPort(PasswordEncoder passwordEncoder) {
        return new PasswordEncoderAdapter(passwordEncoder);
    }
}