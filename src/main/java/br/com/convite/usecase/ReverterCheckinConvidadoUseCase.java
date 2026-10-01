package br.com.convite.usecase;

import br.com.convite.domain.Convite;

public interface ReverterCheckinConvidadoUseCase {
    Convite executar(String codigoOuId);
}
