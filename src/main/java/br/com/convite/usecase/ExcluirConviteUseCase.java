package br.com.convite.usecase;

import br.com.convite.domain.Convite;

public interface ExcluirConviteUseCase {
    Convite executar(String codigoOuId);
}
