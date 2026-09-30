package br.com.convite.usecase;

import br.com.convite.domain.VinculoParticipante;

public interface SalvarVinculoUseCase {
    VinculoParticipante executar(VinculoParticipante dados);
}
