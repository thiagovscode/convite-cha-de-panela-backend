package br.com.convite.usecase.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.BuscarFornecedorPorIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuscarFornecedorPorIdUseCaseImpl implements BuscarFornecedorPorIdUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public Optional<Fornecedor> executar(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return fornecedorGateway.buscarPorId(id.trim());
    }
}
