package br.com.convite.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final int MIN_KEY_BYTES_HS512 = 64; // 512 bits exigidos pela RFC 7518 para HS512

    private SecretKey jwtSecretKey;

    // Validade estrita de 2 horas (2 * 60 * 60 * 1000 = 7.200.000 ms)
    public static final long EXPIRACAO_2_HORAS_MS = 2 * 60 * 60 * 1000L;

    @Value("${JWT_EXPIRATION_MS:#{null}}")
    private Long customExpirationMs;

    @Value("${JWT_SECRET:${app.security.jwt.secret:}}")
    private String configuredSecret;

    public long getExpirationDuration() {
        return (customExpirationMs != null && customExpirationMs > 0)
                ? customExpirationMs
                : EXPIRACAO_2_HORAS_MS;
    }

    @PostConstruct
    public void init() {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            throw new IllegalStateException(
                "FALHA CRÍTICA DE CONFIGURAÇÃO: O segredo JWT não foi configurado. " +
                "Defina a variável de ambiente JWT_SECRET (ou a propriedade 'app.security.jwt.secret') " +
                "com no mínimo 64 bytes (512 bits) de entropia para HS512, preferencialmente codificada em Base64."
            );
        }

        String rawSecret = configuredSecret.trim();
        byte[] keyBytes = extrairBytesSegredo(rawSecret);

        if (keyBytes.length < MIN_KEY_BYTES_HS512) {
            throw new IllegalStateException(String.format(
                "FALHA CRÍTICA DE SEGURANÇA: A chave JWT configurada possui %d bytes, mas o algoritmo HS512 " +
                "exige no mínimo %d bytes (512 bits). Forneça um segredo com entropia suficiente.",
                keyBytes.length, MIN_KEY_BYTES_HS512
            ));
        }

        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("Chave criptográfica JWT (HS512) inicializada com sucesso (tamanho: {} bytes). Validade do token: {} horas ({} ms).",
                keyBytes.length, (getExpirationDuration() / (1000.0 * 3600.0)), getExpirationDuration());
    }

    /**
     * Tenta decodificar a string como Base64 (padrão de chaves criptográficas).
     * Caso não seja Base64 válido ou resulte em tamanho insuficiente, utiliza os bytes brutos em UTF-8.
     */
    private byte[] extrairBytesSegredo(String secret) {
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length >= MIN_KEY_BYTES_HS512) {
                return decoded;
            }
        } catch (DecodingException | IllegalArgumentException ignored) {
            // Não é Base64 padronizado; prossegue tratando como bytes UTF-8
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + getExpirationDuration());

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
        Date expiryDate = new Date(now.getTime() + getExpirationDuration());

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
