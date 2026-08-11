package br.com.convite.usecase.impl;

import br.com.convite.domain.Presente;
import br.com.convite.domain.Reserva;
import br.com.convite.exception.PresenteNaoEncontradoException;
import br.com.convite.gateway.PresenteGateway;
import br.com.convite.gateway.ReservaGateway;
import br.com.convite.usecase.ReservarPresenteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservarPresenteUseCaseImpl implements ReservarPresenteUseCase {

    private final PresenteGateway presenteGateway;
    private final ReservaGateway reservaGateway;

    @Override
    @Transactional
    public Reserva executar(Long presenteId, String guestName) {
        Presente presente = presenteGateway.buscarPorId(presenteId)
                .orElseThrow(() -> new PresenteNaoEncontradoException("Presente não encontrado"));

        // Domain logic: Adicionar reserva valida regras de quantidade máxima
        presente.adicionarReserva();

        Reserva reserva = Reserva.builder()
                .id(UUID.randomUUID().toString())
                .presenteId(presenteId)
                .guestName(guestName)
                .status("RESERVED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        presenteGateway.salvar(presente);
        return reservaGateway.salvar(reserva);
    }
}
