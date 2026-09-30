package br.com.convite.usecase;

import br.com.convite.domain.Fornecedor;

import java.util.List;

public interface ListarFornecedoresUseCase {
    List<Fornecedor> executar();
}
