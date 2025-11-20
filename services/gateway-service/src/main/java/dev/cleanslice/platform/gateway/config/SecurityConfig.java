package dev.cleanslice.platform.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:#{null}}")
    private String jwkSetUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Enable JWT resource server (Keycloak) support but still permitAll for endpoints silently
        // in dev, we'll still permit all, but JWT will be available for mapping into headers
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        if (jwkSetUri != null) {
            return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else {
            // For dev, use a symmetric key
            var secret = "my-secret-key-for-dev-only-should-be-at-least-256-bits-long";
            var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            return NimbusReactiveJwtDecoder.withSecretKey(key).build();
        }
    }

    @Bean
    public WebFilter addUserIdHeaderFilter() {
        // Reads JWT 'sub' claim and adds X-User-Id header for downstream services (if not present)
        return (ServerWebExchange exchange, org.springframework.web.server.WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            var auth = exchange.getPrincipal();
            if (request.getHeaders().containsKey("X-User-Id")) {
                return chain.filter(exchange);
            }
            return auth.flatMap(principal -> {
                if (principal instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken) {
                    var jwt = ((org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken)principal).getToken();
                    var sub = jwt.getSubject();
                    ServerHttpRequest mutated = request.mutate().header("X-User-Id", sub).build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                }
                return chain.filter(exchange);
            }).switchIfEmpty(chain.filter(exchange)); // FIX: Thêm fallback khi không có principal
        };
    }
}
