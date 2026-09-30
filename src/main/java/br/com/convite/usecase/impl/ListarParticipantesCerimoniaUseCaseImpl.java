package br.com.convite.usecase.impl;

import br.com.convite.domain.ParticipanteCerimonia;
import br.com.convite.gateway.ParticipanteCerimoniaGateway;
import br.com.convite.usecase.ListarParticipantesCerimoniaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarParticipantesCerimoniaUseCaseImpl implements ListarParticipantesCerimoniaUseCase {

    private final ParticipanteCerimoniaGateway participanteGateway;

    @Override
    public List<ParticipanteCerimonia> executar() {
        return participanteGateway.listarTodos();
    }
}
