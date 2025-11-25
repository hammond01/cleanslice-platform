package dev.cleanslice.platform.product.infrastructure.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converter for Keycloak JWT tokens to Spring Security Authentication.
 * Extracts roles from Keycloak token and maps them to Spring Security authorities.
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
            defaultGrantedAuthoritiesConverter.convert(jwt).stream(),
            extractResourceRoles(jwt).stream()
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    /**
     * Extract roles from Keycloak JWT token.
     * Keycloak stores roles in "realm_access.roles" claim.
     */
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Collections.emptyList();
        }

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List)) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) rolesObj;
        
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());
    }

    /**
     * Get the principal claim name from JWT.
     * Prefers "preferred_username", falls back to "sub".
     */
    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = jwt.getClaim("preferred_username");
        return claimName != null ? claimName : jwt.getSubject();
    }
}
