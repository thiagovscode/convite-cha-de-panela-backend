package br.com.convite.usecase;

import br.com.convite.domain.VinculoParticipante;

import java.util.List;

public interface ListarVinculosUseCase {
    List<VinculoParticipante> executar();
}
