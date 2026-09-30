package br.com.convite.gateway.impl;

import br.com.convite.domain.RsvpCasamento;
import br.com.convite.gateway.RsvpCasamentoGateway;
import br.com.convite.gateway.persistence.RsvpCasamentoRepository;
import br.com.convite.gateway.persistence.entity.RsvpCasamentoEntity;
import br.com.convite.mapper.RsvpCasamentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RsvpCasamentoGatewayImpl implements RsvpCasamentoGateway {

    private final RsvpCasamentoRepository repository;
    private final RsvpCasamentoMapper mapper;

    @Override
    public RsvpCasamento salvarOuAtualizar(RsvpCasamento rsvp) {
        Optional<RsvpCasamentoEntity> existente = repository.findByTelefone(rsvp.getTelefone());

        RsvpCasamentoEntity entity;
        if (existente.isPresent()) {
            entity = mapper.toEntity(rsvp);
            entity.setId(existente.get().getId());
            entity.setCreatedAt(existente.get().getCreatedAt());
            entity.setUpdatedAt(LocalDateTime.now());
        } else {
            entity = mapper.toEntity(rsvp);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
        }

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public List<RsvpCasamento> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RsvpCasamento> buscarPorTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) return Optional.empty();
        return repository.findByTelefone(telefone.trim()).map(mapper::toDomain);
    }

    @Override
    public void deletar(String id) {
        if (id != null && !id.isBlank()) {
            repository.deleteById(id.trim());
        }
    }
}