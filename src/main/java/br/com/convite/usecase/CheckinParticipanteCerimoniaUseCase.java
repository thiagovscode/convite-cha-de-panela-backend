package br.com.convite.usecase;

import br.com.convite.domain.ParticipanteCerimonia;

public interface CheckinParticipanteCerimoniaUseCase {
    ParticipanteCerimonia executar(String id, Boolean presente);
    ParticipanteCerimonia executar(String id, Boolean presente, String statusCortejo);
}
