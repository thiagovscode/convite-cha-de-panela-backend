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
        return repository.findFirstByCodigoIgnoreCase(codigo.trim()).map(mapper::toDomain);
    }

    @Override
    public Optional<Convite> buscarPorId(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return repository.findById(id.trim()).map(mapper::toDomain);
    }

    @Override
    public Optional<Convite> buscarPorIdECodigo(String id, String codigo) {
        // 1. Tenta matching duplo estrito (ID + Código)
        if (id != null && !id.isBlank() && codigo != null && !codigo.isBlank()) {
            Optional<ConviteCasamentoEntity> byId = repository.findById(id.trim());
            if (byId.isPresent() && byId.get().getCodigo() != null && byId.get().getCodigo().equalsIgnoreCase(codigo.trim())) {
                return byId.map(mapper::toDomain);
            }
            Optional<ConviteCasamentoEntity> byCode = repository.findFirstByCodigoIgnoreCase(codigo.trim());
            if (byCode.isPresent() && byCode.get().getId() != null && byCode.get().getId().equals(id.trim())) {
                return byCode.map(mapper::toDomain);
            }
        }
        // 2. Se falhar ou apenas um foi informado, busca por ID
        if (id != null && !id.isBlank()) {
            Optional<ConviteCasamentoEntity> byId = repository.findById(id.trim());
            if (byId.isPresent()) return byId.map(mapper::toDomain);
        }
        // 3. Fallback para Código
        if (codigo != null && !codigo.isBlank()) {
            Optional<ConviteCasamentoEntity> byCode = repository.findFirstByCodigoIgnoreCase(codigo.trim());
            if (byCode.isPresent()) return byCode.map(mapper::toDomain);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Convite> buscarPorCodigoEId(String codigo, String id) {
        return buscarPorIdECodigo(id, codigo);
    }

    @Override
    public Optional<Convite> buscarPorCodigoOuId(String termo) {
        if (termo == null || termo.isBlank()) return Optional.empty();
        String t = termo.trim();
        // 1. Tenta por código
        Optional<ConviteCasamentoEntity> byCode = repository.findFirstByCodigoIgnoreCase(t);
        if (byCode.isPresent()) return byCode.map(mapper::toDomain);
        // 2. Tenta por ID
        try {
            Optional<ConviteCasamentoEntity> byId = repository.findById(t);
            if (byId.isPresent()) return byId.map(mapper::toDomain);
        } catch (Exception ignored) {
            // Em caso de ID em formato inválido para o MongoDB
        }
        return Optional.empty();
    }

    @Override
    public Optional<br.com.convite.domain.MembroConvite> buscarConvidadoPorCodigoConviteEId(String codigoConvite, String membroId) {
        if (codigoConvite == null || codigoConvite.isBlank() || membroId == null || membroId.isBlank()) {
            return Optional.empty();
        }
        // NÍVEL 1: Busca o convite exclusivamente pelo código
        Optional<Convite> conviteOpt = buscarPorCodigo(codigoConvite.trim());
        if (conviteOpt.isEmpty() || conviteOpt.get().getMembros() == null) {
            return Optional.empty();
        }
        // NÍVEL 2: Busca o membro pelo ID dentro de convite.membros
        return conviteOpt.get().getMembros().stream()
                .filter(m -> m.getId() != null && m.getId().trim().equalsIgnoreCase(membroId.trim()))
                .findFirst();
    }

    @Override
    public Optional<br.com.convite.domain.MembroConvite> buscarConvidadoPorConviteIdEConvidadoId(String conviteIdOuCodigo, String membroId) {
        return buscarConvidadoPorCodigoConviteEId(conviteIdOuCodigo, membroId);
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
