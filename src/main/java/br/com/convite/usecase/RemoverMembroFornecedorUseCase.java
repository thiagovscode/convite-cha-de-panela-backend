package br.com.convite.usecase;

import br.com.convite.domain.Fornecedor;

public interface RemoverMembroFornecedorUseCase {
    Fornecedor executar(String fornecedorId, String membroId);
}
