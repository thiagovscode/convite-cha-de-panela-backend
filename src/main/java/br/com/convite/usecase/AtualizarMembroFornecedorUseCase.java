package br.com.convite.usecase;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;

public interface AtualizarMembroFornecedorUseCase {
    Fornecedor executar(String fornecedorId, String membroId, MembroEquipeFornecedor dados);
}
