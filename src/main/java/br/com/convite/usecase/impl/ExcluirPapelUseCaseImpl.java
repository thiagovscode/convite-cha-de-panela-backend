package br.com.convite.usecase.impl;

import br.com.convite.exception.PapelNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.PapelGateway;
import br.com.convite.usecase.ExcluirPapelUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcluirPapelUseCaseImpl implements ExcluirPapelUseCase {

    private final PapelGateway papelGateway;

    @Override
    public void executar(String id) {
        if (id == null || id.isBlank()) {
            throw new RegraDeNegocioException("Identificador do papel é obrigatório.");
        }

        papelGateway.buscarPorId(id.trim())
                .orElseThrow(() -> new PapelNaoEncontradoException(id));

        papelGateway.excluir(id.trim());
    }
}
