package br.com.convite.usecase.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.exception.FornecedorNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.AdicionarMembroFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdicionarMembroFornecedorUseCaseImpl implements AdicionarMembroFornecedorUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public Fornecedor executar(String fornecedorId, MembroEquipeFornecedor novoMembro) {
        if (fornecedorId == null || fornecedorId.isBlank()) {
            throw new RegraDeNegocioException("Identificador do fornecedor é obrigatório.");
        }
        if (novoMembro == null || novoMembro.getNome() == null || novoMembro.getNome().trim().isBlank()) {
            throw new RegraDeNegocioException("O nome do profissional é obrigatório.");
        }

        Fornecedor f = fornecedorGateway.buscarPorId(fornecedorId.trim())
                .orElseThrow(() -> new FornecedorNaoEncontradoException(fornecedorId));

        if (f.getEquipe() == null) {
            f.setEquipe(new ArrayList<>());
        }

        novoMembro.setId("membro-" + UUID.randomUUID().toString().substring(0, 8));
        novoMembro.setNome(novoMembro.getNome().trim());
        novoMembro.setFuncao((novoMembro.getFuncao() != null && !novoMembro.getFuncao().trim().isBlank())
                ? novoMembro.getFuncao().trim() : "Equipe");
        novoMembro.setPresente(false);

        f.getEquipe().add(novoMembro);
        f.setUpdatedAt(LocalDateTime.now());
        return fornecedorGateway.salvar(f);
    }
}
