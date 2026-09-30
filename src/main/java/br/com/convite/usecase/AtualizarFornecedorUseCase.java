package br.com.convite.usecase;

import br.com.convite.domain.Fornecedor;

public interface AtualizarFornecedorUseCase {
    Fornecedor executar(String id, Fornecedor dados);
}
