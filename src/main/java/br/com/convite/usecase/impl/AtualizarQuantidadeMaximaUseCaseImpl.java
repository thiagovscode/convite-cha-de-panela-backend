package br.com.convite.usecase.impl;

import br.com.convite.domain.Presente;
import br.com.convite.exception.PresenteNaoEncontradoException;
import br.com.convite.gateway.PresenteGateway;
import br.com.convite.usecase.AtualizarQuantidadeMaximaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AtualizarQuantidadeMaximaUseCaseImpl implements AtualizarQuantidadeMaximaUseCase {

    private final PresenteGateway presenteGateway;

    @Override
    public Presente executar(Long presenteId, Integer novaQuantidade) {
        Presente presente = presenteGateway.buscarPorId(presenteId)
                .orElseThrow(() -> new PresenteNaoEncontradoException("Presente não encontrado"));

        presente.setMaxQuantity(novaQuantidade);
        
        // Recalcular se esgotado ou não com a nova quantidade máxima
        presente.setEsgotado(presente.getActiveReservationsCount() >= novaQuantidade);

        return presenteGateway.salvar(presente);
    }
}
