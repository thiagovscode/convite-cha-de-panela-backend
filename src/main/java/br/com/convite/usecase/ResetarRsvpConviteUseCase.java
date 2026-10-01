package br.com.convite.usecase;

import br.com.convite.domain.Convite;

public interface ResetarRsvpConviteUseCase {
    Convite executar(String codigoOuId);
}
