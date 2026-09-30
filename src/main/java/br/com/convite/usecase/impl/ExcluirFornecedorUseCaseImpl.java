package br.com.convite.usecase.impl;

import br.com.convite.exception.FornecedorNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.ExcluirFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcluirFornecedorUseCaseImpl implements ExcluirFornecedorUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public void executar(String id) {
        if (id == null || id.isBlank()) {
            throw new RegraDeNegocioException("Identificador do fornecedor é obrigatório.");
        }
        fornecedorGateway.buscarPorId(id.trim())
                .orElseThrow(() -> new FornecedorNaoEncontradoException(id));
        fornecedorGateway.excluir(id.trim());
    }
}
