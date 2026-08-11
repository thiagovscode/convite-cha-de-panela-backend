package br.com.convite.usecase;

import br.com.convite.domain.Reserva;

public interface ReservarPresenteUseCase {
    Reserva executar(Long presenteId, String guestName);
}
