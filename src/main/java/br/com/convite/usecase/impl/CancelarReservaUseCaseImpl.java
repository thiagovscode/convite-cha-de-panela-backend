package br.com.convite.usecase.impl;

import br.com.convite.domain.Presente;
import br.com.convite.domain.Reserva;
import br.com.convite.exception.PresenteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.PresenteGateway;
import br.com.convite.gateway.ReservaGateway;
import br.com.convite.usecase.CancelarReservaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelarReservaUseCaseImpl implements CancelarReservaUseCase {

    private final ReservaGateway reservaGateway;
    private final PresenteGateway presenteGateway;

    @Override
    @Transactional
    public void executar(String reservaId) {
        Reserva reserva = reservaGateway.buscarPorId(reservaId)
                .orElseThrow(() -> new RegraDeNegocioException("Reserva não encontrada"));

        Presente presente = presenteGateway.buscarPorId(reserva.getPresenteId())
                .orElseThrow(() -> new PresenteNaoEncontradoException("Presente associado não encontrado"));

        presente.removerReserva();
        
        presenteGateway.salvar(presente);
        reservaGateway.deletar(reservaId);
    }
}
