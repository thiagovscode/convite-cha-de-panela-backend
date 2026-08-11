package br.com.convite.gateway.impl;

import br.com.convite.domain.Presente;
import br.com.convite.gateway.PresenteGateway;
import br.com.convite.gateway.persistence.PresenteRepository;
import br.com.convite.gateway.persistence.entity.PresenteEntity;
import br.com.convite.mapper.PresenteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PresenteGatewayImpl implements PresenteGateway {

    private final PresenteRepository presenteRepository;
    private final PresenteMapper presenteMapper;

    @Override
    public List<Presente> listarTodos() {
        return presenteRepository.findAll().stream()
                .map(presenteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Presente> buscarPorId(Long id) {
        return presenteRepository.findById(id)
                .map(presenteMapper::toDomain);
    }

    @Override
    public Presente salvar(Presente presente) {
        PresenteEntity entity = presenteMapper.toEntity(presente);
        return presenteMapper.toDomain(presenteRepository.save(entity));
    }
}
