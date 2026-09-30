package br.com.convite.gateway.impl;

import br.com.convite.domain.Convite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.persistence.ConviteCasamentoRepository;
import br.com.convite.gateway.persistence.entity.ConviteCasamentoEntity;
import br.com.convite.mapper.ConviteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ConviteGatewayImpl implements ConviteGateway {

    private final ConviteCasamentoRepository repository;
    private final ConviteMapper mapper;

    @Override
    public List<Convite> listarTodos() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public Optional<Convite> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return repository.findByCodigoIgnoreCase(codigo.trim()).map(mapper::toDomain);
    }

    @Override
    public Optional<Convite> buscarPorId(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return repository.findById(id.trim()).map(mapper::toDomain);
    }

    @Override
    public List<Convite> buscarPorTermo(String termo) {
        if (termo == null || termo.isBlank()) return List.of();
        // Escapa caracteres especiais de regex antes de passar para o MongoDB
        String termoSeguro = Pattern.quote(termo.trim());
        List<ConviteCasamentoEntity> entities = repository.buscarPorTermoGeral(termoSeguro);
        return mapper.toDomainList(entities);
    }

    @Override
    public List<Convite> buscarPorNomeMembro(String nomeMembro) {
        if (nomeMembro == null || nomeMembro.isBlank()) return List.of();
        // Usa Pattern.quote para busca literal do nome (evita regex injection)
        String nomeRegex = java.util.regex.Pattern.quote(nomeMembro.trim());
        return mapper.toDomainList(repository.findByMembrosNomeRegex(nomeRegex));
    }

    @Override
    public Convite salvar(Convite convite) {
        ConviteCasamentoEntity entity = mapper.toEntity(convite);
        ConviteCasamentoEntity salvo = repository.save(entity);
        return mapper.toDomain(salvo);
    }

    @Override
    public void excluir(Convite convite) {
        ConviteCasamentoEntity entity = mapper.toEntity(convite);
        repository.delete(entity);
    }

    @Override
    public long contarTotal() {
        return repository.count();
    }
}
