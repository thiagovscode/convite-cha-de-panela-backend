package br.com.convite.gateway.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshTokenEntity {

    @Id
    private String id;

    @Indexed
    private String username;

    @Indexed(unique = true)
    private String tokenHash;

    @Indexed
    private String familyId;

    @Indexed
    private Instant expiresAt;

    private Instant createdAt;

    private Instant revokedAt;

    private boolean revoked;

    private String replacedByTokenHash;

    private String deviceInfo;
}
