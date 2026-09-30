package br.com.convite.usecase.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.exception.FornecedorNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.RemoverMembroFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RemoverMembroFornecedorUseCaseImpl implements RemoverMembroFornecedorUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public Fornecedor executar(String fornecedorId, String membroId) {
        if (fornecedorId == null || fornecedorId.isBlank() || membroId == null || membroId.isBlank()) {
            throw new RegraDeNegocioException("Identificadores de fornecedor e membro são obrigatórios.");
        }

        Fornecedor f = fornecedorGateway.buscarPorId(fornecedorId.trim())
                .orElseThrow(() -> new FornecedorNaoEncontradoException(fornecedorId));

        if (f.getEquipe() != null) {
            f.getEquipe().removeIf(m -> m.getId() != null && m.getId().equalsIgnoreCase(membroId.trim()));
            f.setUpdatedAt(LocalDateTime.now());
            return fornecedorGateway.salvar(f);
        }

        return f;
    }
}
