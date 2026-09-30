package br.com.convite.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private String id;
    private String username;
    private String tokenHash;
    private String familyId;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant revokedAt;
    private boolean revoked;
    private String replacedByTokenHash;
    private String deviceInfo;
}
