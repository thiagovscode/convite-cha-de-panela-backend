package br.com.convite.gateway;

import br.com.convite.domain.Fornecedor;

import java.util.List;
import java.util.Optional;

public interface FornecedorGateway {
    List<Fornecedor> listarTodos();
    Optional<Fornecedor> buscarPorId(String id);
    Fornecedor salvar(Fornecedor fornecedor);
    void excluir(String id);
}
