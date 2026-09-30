package br.com.convite.usecase;

import br.com.convite.domain.Convite;

import java.util.List;

public interface BuscarConvitesPorTermoUseCase {
    List<Convite> executar(String termo);
}
