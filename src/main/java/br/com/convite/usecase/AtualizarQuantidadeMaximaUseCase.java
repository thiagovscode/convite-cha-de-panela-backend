package br.com.convite.usecase;

import br.com.convite.domain.Presente;

public interface AtualizarQuantidadeMaximaUseCase {
    Presente executar(Long presenteId, Integer novaQuantidade);
}
