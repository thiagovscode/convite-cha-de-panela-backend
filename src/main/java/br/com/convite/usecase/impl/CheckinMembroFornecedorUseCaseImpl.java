package br.com.convite.usecase.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.exception.EntidadeNaoEncontradaException;
import br.com.convite.exception.FornecedorNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.CheckinMembroFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckinMembroFornecedorUseCaseImpl implements CheckinMembroFornecedorUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public ResultadoCheckin executar(String fornecedorId, String membroId, Boolean presente) {
        if (fornecedorId == null || fornecedorId.isBlank() || membroId == null || membroId.isBlank()) {
            throw new RegraDeNegocioException("Identificadores são obrigatórios.");
        }

        Fornecedor f = fornecedorGateway.buscarPorId(fornecedorId.trim())
                .orElseThrow(() -> new FornecedorNaoEncontradoException(fornecedorId));

        if (f.getEquipe() == null || f.getEquipe().isEmpty()) {
            throw new RegraDeNegocioException("Equipe do fornecedor está vazia.");
        }

        MembroEquipeFornecedor membro = f.getEquipe().stream()
                .filter(m -> m.getId() != null && m.getId().equalsIgnoreCase(membroId.trim()))
                .findFirst()
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Profissional não localizado na equipe: " + membroId));

        boolean novoStatus = (presente != null) ? presente : !Boolean.TRUE.equals(membro.getPresente());
        membro.setPresente(novoStatus);
        membro.setDataHoraEntrada(novoStatus ? LocalDateTime.now() : null);
        f.setUpdatedAt(LocalDateTime.now());

        Fornecedor salvo = fornecedorGateway.salvar(f);
        return new ResultadoCheckin(salvo, membro);
    }
}
