package br.com.convite.usecase;

import br.com.convite.domain.Convite;

import java.util.List;

public interface ListarConvitesUseCase {
    List<Convite> executar();
}
