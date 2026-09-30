package br.com.convite.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final int MIN_KEY_BYTES_HS512 = 64; // 512 bits exigidos pela RFC 7518 para HS512

    private SecretKey jwtSecretKey;

    // Padrão de expiração curta para o Access Token: 15 minutos (900.000 ms)
    public static final long DEFAULT_ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000L;

    // Padrão de expiração longa para o Refresh Token: 7 dias (604.800.000 ms)
    public static final long DEFAULT_REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    @Value("${JWT_ACCESS_TOKEN_EXPIRATION_MS:${jwt.access-token-expiration:#{null}}}")
    private Long configuredAccessTokenExpirationMs;

    @Value("${JWT_REFRESH_TOKEN_EXPIRATION_MS:${jwt.refresh-token-expiration:#{null}}}")
    private Long configuredRefreshTokenExpirationMs;

    @Value("${JWT_SECRET:${JWT_SECRET_BASE64:${app.security.jwt.secret:}}}")
    private String configuredSecret;

    public long getAccessTokenExpirationMs() {
        return (configuredAccessTokenExpirationMs != null && configuredAccessTokenExpirationMs > 0)
                ? configuredAccessTokenExpirationMs
                : DEFAULT_ACCESS_TOKEN_EXPIRATION_MS;
    }

    public long getRefreshTokenExpirationMs() {
        return (configuredRefreshTokenExpirationMs != null && configuredRefreshTokenExpirationMs > 0)
                ? configuredRefreshTokenExpirationMs
                : DEFAULT_REFRESH_TOKEN_EXPIRATION_MS;
    }

    public long getExpirationDuration() {
        return getAccessTokenExpirationMs();
    }

    @PostConstruct
    public void init() {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            // Fallback resiliente: gera chave criptográfica HS512 segura de 512 bits em memória.
            // Isso garante que a aplicação inicialize e atenda requisições mesmo se JWT_SECRET ainda não tiver sido setado no Elastic Beanstalk.
            this.jwtSecretKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
            log.warn("AVISO: JWT_SECRET não foi configurado nas variáveis de ambiente. " +
                     "Uma chave segura HS512 (512 bits) foi gerada automaticamente para esta sessão.");
            log.info("Chave criptográfica JWT auto-gerada (HS512). Expiração Access Token: {}s. Expiração Refresh Token: {}s.",
                    (getAccessTokenExpirationMs() / 1000), (getRefreshTokenExpirationMs() / 1000));
            return;
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
        log.info("Chave criptográfica JWT (HS512) inicializada com sucesso ({} bytes). Expiração do Access Token: {}s ({} ms). Expiração do Refresh Token: {}s ({} ms).",
                keyBytes.length,
                (getAccessTokenExpirationMs() / 1000), getAccessTokenExpirationMs(),
                (getRefreshTokenExpirationMs() / 1000), getRefreshTokenExpirationMs());
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

        return generateTokenForUsername(username, role);
    }

    /**
     * Gera um Access Token JWT com todas as claims recomendadas:
     * - sub (subject = username)
     * - role
     * - token_type = "access" (impede confusão com refresh token)
     * - jti (identificador único do token)
     * - iat (data de emissão)
     * - exp (data de expiração curta)
     */
    public String generateTokenForUsername(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + getAccessTokenExpirationMs());

        String roleClaim = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + (role != null ? role.toUpperCase() : "USER");
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(username)
                .id(jti)
                .claim("role", roleClaim)
                .claim("token_type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();
    }

    public Claims parseAndValidateClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return parseAndValidateClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        try {
            return parseAndValidateClaims(token).get("role", String.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public String getJtiFromToken(String token) {
        try {
            return parseAndValidateClaims(token).getId();
        } catch (Exception ex) {
            return null;
        }
    }

    public String getTokenTypeFromToken(String token) {
        try {
            return parseAndValidateClaims(token).get("token_type", String.class);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Valida a integridade, assinatura e validade do Access Token.
     * Garante também que o token seja do tipo "access".
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseAndValidateClaims(token);
            String tokenType = claims.get("token_type", String.class);
            // Tokens sem token_type ou com tipo diferente de "access" são rejeitados
            return "access".equalsIgnoreCase(tokenType);
        } catch (ExpiredJwtException ex) {
            log.debug("Token JWT expirado: {}", ex.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token JWT inválido ou adulterado: {}", ex.getMessage());
            return false;
        }
    }
}
