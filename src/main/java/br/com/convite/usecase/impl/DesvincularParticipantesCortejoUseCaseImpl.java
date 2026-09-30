package br.com.convite.usecase.impl;

import br.com.convite.domain.ParticipanteCerimonia;
import br.com.convite.gateway.ParticipanteCerimoniaGateway;
import br.com.convite.usecase.DesvincularParticipantesCortejoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DesvincularParticipantesCortejoUseCaseImpl implements DesvincularParticipantesCortejoUseCase {

    private final ParticipanteCerimoniaGateway participanteGateway;

    @Override
    public void executar(String codigoConvite) {
        if (codigoConvite == null || codigoConvite.isBlank()) return;

        List<ParticipanteCerimonia> participantes = participanteGateway.buscarPorCodigoConvite(codigoConvite.trim());
        for (ParticipanteCerimonia p : participantes) {
            p.setCodigoConvite(null);
            p.setConfirmadoRsvp(false);
            p.setUpdatedAt(LocalDateTime.now());
            participanteGateway.salvar(p);
        }
    }
}
