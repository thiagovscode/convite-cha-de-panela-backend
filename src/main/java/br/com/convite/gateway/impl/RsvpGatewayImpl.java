package br.com.convite.gateway.impl;

import br.com.convite.domain.Rsvp;
import br.com.convite.gateway.RsvpGateway;
import br.com.convite.gateway.persistence.RsvpRepository;
import br.com.convite.gateway.persistence.entity.RsvpEntity;
import br.com.convite.mapper.RsvpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RsvpGatewayImpl implements RsvpGateway {

    private final RsvpRepository rsvpRepository;
    private final RsvpMapper rsvpMapper;

    @Override
    public Rsvp salvar(Rsvp rsvp) {
        RsvpEntity entity = rsvpMapper.toEntity(rsvp);
        return rsvpMapper.toDomain(rsvpRepository.save(entity));
    }

    @Override
    public List<Rsvp> listarTodos() {
        return rsvpRepository.findAll().stream()
                .map(rsvpMapper::toDomain)
                .collect(Collectors.toList());
    }
}
