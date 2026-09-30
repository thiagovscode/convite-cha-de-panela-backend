package br.com.convite.gateway.impl;

import br.com.convite.gateway.RefreshTokenGateway;
import br.com.convite.gateway.persistence.RefreshTokenRepository;
import br.com.convite.gateway.persistence.entity.RefreshTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenGatewayImpl implements RefreshTokenGateway {

    private final RefreshTokenRepository repository;

    @Override
    public RefreshTokenEntity salvar(RefreshTokenEntity token) {
        return repository.save(token);
    }

    @Override
    public Optional<RefreshTokenEntity> buscarPorHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return Optional.empty();
        }
        return repository.findByTokenHash(tokenHash.trim());
    }

    @Override
    public List<RefreshTokenEntity> buscarTodosPorUsername(String username) {
        if (username == null || username.isBlank()) {
            return List.of();
        }
        return repository.findAllByUsername(username.trim());
    }

    @Override
    public List<RefreshTokenEntity> buscarTodosPorFamilia(String familyId) {
        if (familyId == null || familyId.isBlank()) {
            return List.of();
        }
        return repository.findAllByFamilyId(familyId.trim());
    }

    @Override
    public void revogarFamilia(String familyId) {
        if (familyId == null || familyId.isBlank()) return;
        List<RefreshTokenEntity> tokens = repository.findAllByFamilyId(familyId.trim());
        Instant agora = Instant.now();
        for (RefreshTokenEntity t : tokens) {
            if (!t.isRevoked()) {
                t.setRevoked(true);
                t.setRevokedAt(agora);
                repository.save(t);
            }
        }
    }

    @Override
    public void revogarTodosPorUsername(String username) {
        if (username == null || username.isBlank()) return;
        List<RefreshTokenEntity> tokens = repository.findAllByUsername(username.trim());
        Instant agora = Instant.now();
        for (RefreshTokenEntity t : tokens) {
            if (!t.isRevoked()) {
                t.setRevoked(true);
                t.setRevokedAt(agora);
                repository.save(t);
            }
        }
    }

    @Override
    public void excluirExpiradosAntesDe(Instant data) {
        if (data == null) return;
        repository.deleteAllByExpiresAtBefore(data);
    }
}
