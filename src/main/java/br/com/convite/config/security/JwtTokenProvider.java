package br.com.convite.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import org.springframework.security.core.GrantedAuthority;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private SecretKey jwtSecretKey;

    @Value("${app.security.jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${JWT_SECRET:${app.security.jwt.secret:}}")
    private String configuredSecret;

    @PostConstruct
    public void init() {
        String secretToUse = (configuredSecret != null && configuredSecret.trim().length() >= 32)
                ? configuredSecret.trim()
                : "CasamentoTainaraThiago2027BalboaMairiporaSegredoJwtHMAC512ChaveSeguraExclusiva2026!";

        byte[] keyBytes = secretToUse.getBytes(StandardCharsets.UTF_8);
        byte[] padded = Arrays.copyOf(keyBytes, Math.max(64, keyBytes.length));
        this.jwtSecretKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(padded);
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();
    }

    public String generateTokenForUsername(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String roleClaim = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + (role != null ? role.toUpperCase() : "USER");

        return Jwts.builder()
                .subject(username)
                .claim("role", roleClaim)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("role", String.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(jwtSecretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
