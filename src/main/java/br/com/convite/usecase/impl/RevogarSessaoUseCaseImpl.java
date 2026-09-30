package br.com.convite.usecase.impl;

import br.com.convite.config.security.TokenHashUtil;
import br.com.convite.gateway.RefreshTokenGateway;
import br.com.convite.gateway.persistence.entity.RefreshTokenEntity;
import br.com.convite.usecase.RevogarSessaoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevogarSessaoUseCaseImpl implements RevogarSessaoUseCase {

    private final RefreshTokenGateway refreshTokenGateway;

    @Override
    public void revogarPorRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.trim().isBlank()) {
            return;
        }

        try {
            String tokenHash = TokenHashUtil.calcularSha256(rawRefreshToken.trim());
            Optional<RefreshTokenEntity> tokenOpt = refreshTokenGateway.buscarPorHash(tokenHash);

            tokenOpt.ifPresent(token -> {
                if (!token.isRevoked()) {
                    token.setRevoked(true);
                    token.setRevokedAt(Instant.now());
                    refreshTokenGateway.salvar(token);
                    log.info("Refresh token revogado com sucesso para o usuário '{}'.", token.getUsername());
                }
            });
        } catch (Exception e) {
            log.warn("Erro ao tentar revogar refresh token: {}", e.getMessage());
        }
    }

    @Override
    public void revogarTodasDoUsuario(String username) {
        if (username == null || username.trim().isBlank()) {
            return;
        }
        String cleanUser = username.trim();
        refreshTokenGateway.revogarTodosPorUsername(cleanUser);
        log.info("Todas as sessões ativas do usuário '{}' foram revogadas.", cleanUser);
    }
}
