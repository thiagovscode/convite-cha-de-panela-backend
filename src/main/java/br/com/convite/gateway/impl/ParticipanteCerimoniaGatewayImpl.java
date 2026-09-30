package br.com.convite.gateway.impl;

import br.com.convite.domain.ParticipanteCerimonia;
import br.com.convite.gateway.ParticipanteCerimoniaGateway;
import br.com.convite.gateway.persistence.ParticipanteCerimoniaRepository;
import br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity;
import br.com.convite.mapper.ParticipanteCerimoniaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ParticipanteCerimoniaGatewayImpl implements ParticipanteCerimoniaGateway {

    private final ParticipanteCerimoniaRepository repository;
    private final ParticipanteCerimoniaMapper mapper;

    @Override
    public List<ParticipanteCerimonia> listarTodos() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public Optional<ParticipanteCerimonia> buscarPorId(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return repository.findById(id.trim()).map(mapper::toDomain);
    }

    @Override
    public List<ParticipanteCerimonia> buscarPorCodigoConvite(String codigoConvite) {
        if (codigoConvite == null || codigoConvite.isBlank()) return List.of();
        List<ParticipanteCerimoniaEntity> entities = repository.findByCodigoConviteIgnoreCase(codigoConvite.trim());
        return mapper.toDomainList(entities);
    }

    @Override
    public Optional<ParticipanteCerimonia> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) return Optional.empty();
        return repository.findByNomeIgnoreCase(nome.trim()).map(mapper::toDomain);
    }

    @Override
    public ParticipanteCerimonia salvar(ParticipanteCerimonia participante) {
        ParticipanteCerimoniaEntity entity = mapper.toEntity(participante);
        ParticipanteCerimoniaEntity salvo = repository.save(entity);
        return mapper.toDomain(salvo);
    }

    @Override
    public void excluirPorId(String id) {
        if (id != null && !id.isBlank()) {
            repository.deleteById(id.trim());
        }
    }
}
