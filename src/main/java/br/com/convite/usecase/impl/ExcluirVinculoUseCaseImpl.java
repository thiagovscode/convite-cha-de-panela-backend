package br.com.convite.usecase.impl;

import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.exception.VinculoNaoEncontradoException;
import br.com.convite.gateway.VinculoGateway;
import br.com.convite.usecase.ExcluirVinculoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcluirVinculoUseCaseImpl implements ExcluirVinculoUseCase {

    private final VinculoGateway vinculoGateway;

    @Override
    public void executar(String id) {
        if (id == null || id.isBlank()) {
            throw new RegraDeNegocioException("Identificador do vínculo é obrigatório.");
        }

        vinculoGateway.buscarPorId(id.trim())
                .orElseThrow(() -> new VinculoNaoEncontradoException(id));

        vinculoGateway.excluir(id.trim());
    }
}
