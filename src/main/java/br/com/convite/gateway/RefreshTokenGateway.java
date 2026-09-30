package br.com.convite.gateway;

import br.com.convite.gateway.persistence.entity.RefreshTokenEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenGateway {

    RefreshTokenEntity salvar(RefreshTokenEntity token);

    Optional<RefreshTokenEntity> buscarPorHash(String tokenHash);

    List<RefreshTokenEntity> buscarTodosPorUsername(String username);

    List<RefreshTokenEntity> buscarTodosPorFamilia(String familyId);

    void revogarFamilia(String familyId);

    void revogarTodosPorUsername(String username);

    void excluirExpiradosAntesDe(Instant data);
}
