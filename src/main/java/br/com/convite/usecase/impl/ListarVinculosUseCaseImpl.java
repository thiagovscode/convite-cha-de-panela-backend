package br.com.convite.usecase.impl;

import br.com.convite.domain.VinculoParticipante;
import br.com.convite.gateway.VinculoGateway;
import br.com.convite.usecase.ListarVinculosUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarVinculosUseCaseImpl implements ListarVinculosUseCase {

    private final VinculoGateway vinculoGateway;

    @Override
    public List<VinculoParticipante> executar() {
        return vinculoGateway.listarTodos().stream()
                .sorted(Comparator.comparing(v -> v.getNome() != null ? v.getNome() : ""))
                .collect(Collectors.toList());
    }
}
