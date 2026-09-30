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
        String telNorm = (rsvp.getTelefone() != null) ? rsvp.getTelefone().replaceAll("\\D", "") : "";
        if (rsvp.getTelefone() != null) {
            rsvp.setTelefone(telNorm);
        }
        Optional<RsvpCasamentoEntity> existente = repository.findFirstByTelefone(telNorm);
        if (existente.isEmpty() && rsvp.getTelefone() != null && !rsvp.getTelefone().isBlank()) {
            existente = repository.findFirstByTelefone(rsvp.getTelefone().trim());
        }

        RsvpCasamentoEntity entity = mapper.toEntity(rsvp);
        if (existente.isPresent()) {
            entity.setId(existente.get().getId());
            entity.setCreatedAt(existente.get().getCreatedAt());
        } else {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setTelefone(telNorm);

        try {
            return mapper.toDomain(repository.save(entity));
        } catch (org.springframework.dao.DuplicateKeyException dke) {
            Optional<RsvpCasamentoEntity> conflito = repository.findFirstByTelefone(telNorm);
            if (conflito.isPresent()) {
                entity.setId(conflito.get().getId());
                entity.setCreatedAt(conflito.get().getCreatedAt());
                return mapper.toDomain(repository.save(entity));
            }
            throw dke;
        }
    }

    @Override
    public List<RsvpCasamento> listarTodos() {
        try {
            return repository.findAll().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return List.of();
        }
    }

    @Override
    public Optional<RsvpCasamento> buscarPorTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) return Optional.empty();
        String telNorm = telefone.replaceAll("\\D", "");
        Optional<RsvpCasamento> result = repository.findFirstByTelefone(telNorm).map(mapper::toDomain);
        if (result.isEmpty()) {
            result = repository.findFirstByTelefone(telefone.trim()).map(mapper::toDomain);
        }
        return result;
    }

    @Override
    public void deletar(String id) {
        if (id != null && !id.isBlank()) {
            repository.deleteById(id.trim());
        }
    }
}