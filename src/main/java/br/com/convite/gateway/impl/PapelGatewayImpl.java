package br.com.convite.gateway.impl;

import br.com.convite.domain.PapelParticipante;
import br.com.convite.gateway.PapelGateway;
import br.com.convite.gateway.persistence.PapelParticipanteRepository;
import br.com.convite.mapper.PapelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PapelGatewayImpl implements PapelGateway {

    private final PapelParticipanteRepository repository;
    private final PapelMapper mapper;

    @Override
    public List<PapelParticipante> listarTodos() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public Optional<PapelParticipante> buscarPorId(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PapelParticipante> buscarPorNome(String nome) {
        return repository.findByNomeIgnoreCase(nome).map(mapper::toDomain);
    }

    @Override
    public PapelParticipante salvar(PapelParticipante papel) {
        var entity = mapper.toEntity(papel);
        var salvo = repository.save(entity);
        return mapper.toDomain(salvo);
    }

    @Override
    public void excluir(String id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existePorNome(String nome) {
        return repository.existsByNomeIgnoreCase(nome);
    }
}
