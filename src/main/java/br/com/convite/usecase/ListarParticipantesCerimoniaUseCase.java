package br.com.convite.usecase;

import br.com.convite.domain.ParticipanteCerimonia;

import java.util.List;

public interface ListarParticipantesCerimoniaUseCase {
    List<ParticipanteCerimonia> executar();
}
