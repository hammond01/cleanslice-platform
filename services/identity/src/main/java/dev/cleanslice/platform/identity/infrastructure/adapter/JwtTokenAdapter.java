package dev.cleanslice.platform.identity.infrastructure.adapter;

import dev.cleanslice.platform.identity.application.port.JwtTokenPort;
import dev.cleanslice.platform.identity.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT token adapter implementation.
 */
@Component
public class JwtTokenAdapter implements JwtTokenPort {

    private final String secret;
    private final long expiration;
    private final Key key;

    public JwtTokenAdapter(@Value("${jwt.secret}") String secret,
                          @Value("${jwt.expiration}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    @Override
    public User getUserFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        // Note: This is a simplified reconstruction - in real app you'd fetch from DB
        return new User(
            (java.util.UUID) claims.get("userId"),
            claims.getSubject(),
            (String) claims.get("email"),
            "",
            null,
            null,
            true
        );
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}