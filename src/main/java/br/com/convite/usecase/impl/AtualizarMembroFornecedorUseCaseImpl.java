package br.com.convite.usecase.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.exception.FornecedorNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.AtualizarMembroFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AtualizarMembroFornecedorUseCaseImpl implements AtualizarMembroFornecedorUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public Fornecedor executar(String fornecedorId, String membroId, MembroEquipeFornecedor dados) {
        if (fornecedorId == null || fornecedorId.isBlank()) {
            throw new RegraDeNegocioException("ID do fornecedor é obrigatório.");
        }
        if (membroId == null || membroId.isBlank()) {
            throw new RegraDeNegocioException("ID do membro é obrigatório.");
        }

        Fornecedor fornecedor = fornecedorGateway.buscarPorId(fornecedorId.trim())
                .orElseThrow(() -> new FornecedorNaoEncontradoException(fornecedorId));

        if (fornecedor.getEquipe() != null && dados != null) {
            for (MembroEquipeFornecedor m : fornecedor.getEquipe()) {
                if (membroId.equalsIgnoreCase(m.getId())) {
                    if (dados.getNome() != null && !dados.getNome().isBlank()) m.setNome(dados.getNome().trim());
                    if (dados.getFuncao() != null) m.setFuncao(dados.getFuncao().trim());
                    if (dados.getPermaneceAteFim() != null) m.setPermaneceAteFim(dados.getPermaneceAteFim());
                    if (dados.getPresente() != null) m.setPresente(dados.getPresente());
                }
            }
        }

        fornecedor.setUpdatedAt(LocalDateTime.now());
        return fornecedorGateway.salvar(fornecedor);
    }
}
