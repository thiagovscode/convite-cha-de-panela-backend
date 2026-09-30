package br.com.convite.gateway.impl;

import br.com.convite.domain.VinculoParticipante;
import br.com.convite.gateway.VinculoGateway;
import br.com.convite.gateway.persistence.VinculoParticipanteRepository;
import br.com.convite.mapper.VinculoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VinculoGatewayImpl implements VinculoGateway {

    private final VinculoParticipanteRepository repository;
    private final VinculoMapper mapper;

    @Override
    public List<VinculoParticipante> listarTodos() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public Optional<VinculoParticipante> buscarPorId(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<VinculoParticipante> buscarPorNome(String nome) {
        return repository.findByNomeIgnoreCase(nome).map(mapper::toDomain);
    }

    @Override
    public VinculoParticipante salvar(VinculoParticipante vinculo) {
        var entity = mapper.toEntity(vinculo);
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
