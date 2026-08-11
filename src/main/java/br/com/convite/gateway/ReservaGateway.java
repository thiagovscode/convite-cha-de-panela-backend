package br.com.convite.gateway;

import br.com.convite.domain.Reserva;
import java.util.List;
import java.util.Optional;

public interface ReservaGateway {
    Reserva salvar(Reserva reserva);
    List<Reserva> buscarPorPresenteId(Long presenteId);
    Optional<Reserva> buscarPorId(String id);
    void deletar(String id);
}
