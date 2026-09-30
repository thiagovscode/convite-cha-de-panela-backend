package br.com.convite.usecase.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.ListarFornecedoresUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarFornecedoresUseCaseImpl implements ListarFornecedoresUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public List<Fornecedor> executar() {
        return fornecedorGateway.listarTodos();
    }
}
