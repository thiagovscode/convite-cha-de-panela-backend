package br.com.convite.usecase;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;

public interface CheckinMembroFornecedorUseCase {
    record ResultadoCheckin(Fornecedor fornecedor, MembroEquipeFornecedor membro) {}
    ResultadoCheckin executar(String fornecedorId, String membroId, Boolean presente);
}
