package br.com.convite.usecase;

import br.com.convite.domain.Fornecedor;

import java.util.Optional;

public interface BuscarFornecedorPorIdUseCase {
    Optional<Fornecedor> executar(String id);
}
