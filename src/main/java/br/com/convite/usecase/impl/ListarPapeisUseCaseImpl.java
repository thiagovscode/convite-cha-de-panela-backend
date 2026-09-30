package br.com.convite.usecase.impl;

import br.com.convite.domain.PapelParticipante;
import br.com.convite.gateway.PapelGateway;
import br.com.convite.usecase.ListarPapeisUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarPapeisUseCaseImpl implements ListarPapeisUseCase {

    private final PapelGateway papelGateway;

    @Override
    public List<PapelParticipante> executar() {
        return papelGateway.listarTodos().stream()
                .sorted(Comparator.comparing(p -> p.getNome() != null ? p.getNome() : ""))
                .collect(Collectors.toList());
    }
}
