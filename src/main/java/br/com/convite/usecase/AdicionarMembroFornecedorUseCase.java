package br.com.convite.usecase;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;

public interface AdicionarMembroFornecedorUseCase {
    Fornecedor executar(String fornecedorId, MembroEquipeFornecedor novoMembro);
}
