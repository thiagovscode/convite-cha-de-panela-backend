package br.com.convite.usecase;

import br.com.convite.domain.PapelParticipante;

import java.util.List;

public interface ListarPapeisUseCase {
    List<PapelParticipante> executar();
}
