package br.com.convite.usecase;

import br.com.convite.domain.PapelParticipante;

public interface SalvarPapelUseCase {
    PapelParticipante executar(PapelParticipante dados);
}
