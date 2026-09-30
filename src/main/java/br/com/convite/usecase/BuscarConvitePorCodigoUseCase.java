package br.com.convite.usecase;

import br.com.convite.domain.Convite;

import java.util.Optional;

public interface BuscarConvitePorCodigoUseCase {
    Optional<Convite> executar(String codigo);
}
