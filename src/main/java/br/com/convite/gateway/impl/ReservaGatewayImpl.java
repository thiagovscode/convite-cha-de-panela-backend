package br.com.convite.gateway.impl;

import br.com.convite.domain.Reserva;
import br.com.convite.gateway.ReservaGateway;
import br.com.convite.gateway.persistence.ReservaRepository;
import br.com.convite.gateway.persistence.entity.ReservaEntity;
import br.com.convite.mapper.ReservaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservaGatewayImpl implements ReservaGateway {

    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;

    @Override
    public Reserva salvar(Reserva reserva) {
        ReservaEntity entity = reservaMapper.toEntity(reserva);
        return reservaMapper.toDomain(reservaRepository.save(entity));
    }

    @Override
    public List<Reserva> buscarPorPresenteId(Long presenteId) {
        return reservaRepository.findByPresenteId(presenteId).stream()
                .map(reservaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reserva> buscarPorId(String id) {
        return reservaRepository.findById(id)
                .map(reservaMapper::toDomain);
    }

    @Override
    public void deletar(String id) {
        reservaRepository.deleteById(id);
    }
}
