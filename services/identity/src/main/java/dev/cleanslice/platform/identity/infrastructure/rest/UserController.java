package dev.cleanslice.platform.identity.infrastructure.rest;

import dev.cleanslice.platform.identity.application.port.JwtTokenPort;
import dev.cleanslice.platform.identity.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for user operations.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management API")
@Slf4j
public class UserController {

    private final JwtTokenPort jwtTokenPort;

    public UserController(JwtTokenPort jwtTokenPort) {
        this.jwtTokenPort = jwtTokenPort;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            User user = jwtTokenPort.getUserFromToken(token);

            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        }
    }
}