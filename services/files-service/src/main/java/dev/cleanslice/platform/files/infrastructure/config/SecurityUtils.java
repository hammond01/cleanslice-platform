package dev.cleanslice.platform.files.infrastructure.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

/**
 * Utility class to extract authenticated user information from JWT token.
 */
public class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Get the current authenticated user's ID from JWT token.
     * Extracts from 'sub' claim in the JWT.
     * 
     * @return UUID of the authenticated user
     * @throws IllegalStateException if no authentication or invalid format
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            throw new IllegalStateException("No authentication found in security context");
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String subject = jwt.getSubject();
            
            // Try to extract user_id claim first (custom mapper)
            String userId = jwt.getClaimAsString("user_id");
            if (userId != null) {
                return UUID.fromString(userId);
            }
            
            // Fallback to sub claim
            if (subject != null) {
                try {
                    return UUID.fromString(subject);
                } catch (IllegalArgumentException e) {
                    // If sub is not UUID, might be username - handle differently
                    throw new IllegalStateException("Subject claim is not a valid UUID: " + subject);
                }
            }
        }

        // Fallback: try to get from X-User-Id header (for development mode)
        throw new IllegalStateException("Unable to extract user ID from JWT");
    }

    /**
     * Get the current authenticated username from JWT token.
     * 
     * @return username of the authenticated user
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return null;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null) {
                return username;
            }
            return jwt.getSubject();
        }

        return authentication.getName();
    }

    /**
     * Check if the current user has a specific role.
     * 
     * @param role the role to check
     * @return true if user has the role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }
}
