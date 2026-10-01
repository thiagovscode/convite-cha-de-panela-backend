package br.com.convite.gateway.impl;

import br.com.convite.domain.Convite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.persistence.ConviteCasamentoRepository;
import br.com.convite.gateway.persistence.entity.ConviteCasamentoEntity;
import br.com.convite.mapper.ConviteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ConviteGatewayImpl implements ConviteGateway {

    private final ConviteCasamentoRepository repository;
    private final ConviteMapper mapper;
    private final MongoTemplate mongoTemplate;

    public ConviteGatewayImpl(
            ConviteCasamentoRepository repository,
            ConviteMapper mapper,
            @Autowired(required = false) MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
    }

    private Convite sanitizarConvite(Convite convite) {
        if (convite == null) return null;
        if (convite.getMembros() != null) {
            for (br.com.convite.domain.MembroConvite m : convite.getMembros()) {
                if (m.getId() == null) {
                    m.setId(UUID.randomUUID());
                }
                if (m.getPapel() == null || m.getPapel().isBlank()) {
                    m.setPapel("Convidado");
                }
            }
        }
        return convite;
    }

    private List<Convite> sanitizarLista(List<Convite> lista) {
        if (lista == null) return Collections.emptyList();
        for (Convite c : lista) {
            sanitizarConvite(c);
        }
        return lista;
    }

    @Override
    public List<Convite> listarTodos() {
        try {
            return sanitizarLista(mapper.toDomainList(repository.findAll()));
        } catch (Exception ex) {
            log.error("Erro ao listar convites via repository.findAll(): {}. Ativando recuperação documento a documento...", ex.getMessage());
            if (mongoTemplate != null) {
                return sanitizarLista(listarTodosResiliente());
            }
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Convite> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return repository.findFirstByCodigoIgnoreCase(codigo.trim()).map(mapper::toDomain).map(this::sanitizarConvite);
    }

    @Override
    public Optional<Convite> buscarPorId(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return repository.findById(id.trim()).map(mapper::toDomain).map(this::sanitizarConvite);
    }

    @Override
    public Optional<Convite> buscarPorIdECodigo(String id, String codigo) {
        // 1. Tenta matching duplo estrito (ID + Código)
        if (id != null && !id.isBlank() && codigo != null && !codigo.isBlank()) {
            Optional<ConviteCasamentoEntity> byId = repository.findById(id.trim());
            if (byId.isPresent() && byId.get().getCodigo() != null && byId.get().getCodigo().equalsIgnoreCase(codigo.trim())) {
                return byId.map(mapper::toDomain).map(this::sanitizarConvite);
            }
            Optional<ConviteCasamentoEntity> byCode = repository.findFirstByCodigoIgnoreCase(codigo.trim());
            if (byCode.isPresent() && byCode.get().getId() != null && byCode.get().getId().equals(id.trim())) {
                return byCode.map(mapper::toDomain).map(this::sanitizarConvite);
            }
        }
        // 2. Se falhar ou apenas um foi informado, busca por ID
        if (id != null && !id.isBlank()) {
            Optional<ConviteCasamentoEntity> byId = repository.findById(id.trim());
            if (byId.isPresent()) return byId.map(mapper::toDomain).map(this::sanitizarConvite);
        }
        // 3. Fallback para Código
        if (codigo != null && !codigo.isBlank()) {
            Optional<ConviteCasamentoEntity> byCode = repository.findFirstByCodigoIgnoreCase(codigo.trim());
            if (byCode.isPresent()) return byCode.map(mapper::toDomain).map(this::sanitizarConvite);
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
        if (byCode.isPresent()) return byCode.map(mapper::toDomain).map(this::sanitizarConvite);
        // 2. Tenta por ID
        try {
            Optional<ConviteCasamentoEntity> byId = repository.findById(t);
            if (byId.isPresent()) return byId.map(mapper::toDomain).map(this::sanitizarConvite);
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
                .filter(m -> m.getId() != null && m.getId().toString().trim().equalsIgnoreCase(membroId.trim()))
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
        return sanitizarLista(mapper.toDomainList(entities));
    }

    @Override
    public List<Convite> buscarPorNomeMembro(String nomeMembro) {
        if (nomeMembro == null || nomeMembro.isBlank()) return List.of();
        // Usa Pattern.quote para busca literal do nome (evita regex injection)
        String nomeRegex = java.util.regex.Pattern.quote(nomeMembro.trim());
        return sanitizarLista(mapper.toDomainList(repository.findByMembrosNomeRegex(nomeRegex)));
    }

    @Override
    public Convite salvar(Convite convite) {
        Convite sanitizado = sanitizarConvite(convite);
        ConviteCasamentoEntity entity = mapper.toEntity(sanitizado);
        ConviteCasamentoEntity salvo = repository.save(entity);
        return sanitizarConvite(mapper.toDomain(salvo));
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

    private List<Convite> listarTodosResiliente() {
        List<Convite> resultado = new ArrayList<>();
        try {
            for (org.bson.Document doc : mongoTemplate.getCollection("convites").find()) {
                try {
                    ConviteCasamentoEntity entity = mongoTemplate.getConverter().read(ConviteCasamentoEntity.class, doc);
                    resultado.add(mapper.toDomain(entity));
                } catch (Exception docEx) {
                    log.warn("Documento de convite com formato divergente recuperado com mapeamento seguro [_id: {}, codigo: {}]: {}",
                            doc.get("_id"), doc.get("codigo"), docEx.getMessage());
                    try {
                        resultado.add(recuperarConviteDeDocumentoBson(doc));
                    } catch (Exception fallbackEx) {
                        log.error("Falha ao recuperar convite básico [_id: {}]: {}", doc.get("_id"), fallbackEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Falha critica no fallback resiliente de convites: {}", e.getMessage(), e);
        }
        return resultado;
    }

    private Convite recuperarConviteDeDocumentoBson(org.bson.Document doc) {
        String id = doc.get("_id") != null ? String.valueOf(doc.get("_id")) : null;
        String codigo = doc.getString("codigo");
        String familia = doc.getString("familia");
        String telefone = doc.getString("telefone");
        String email = doc.getString("email");
        String status = doc.getString("status") != null ? doc.getString("status") : "PENDENTE";
        String observacao = doc.getString("observacao");

        List<br.com.convite.domain.MembroConvite> membros = new ArrayList<>();
        Object membrosRaw = doc.get("membros");
        if (membrosRaw instanceof List<?> lista) {
            for (Object item : lista) {
                if (item instanceof org.bson.Document mDoc) {
                    Object rawId = mDoc.get("_id") != null ? mDoc.get("_id") : mDoc.get("id");
                    UUID mIdUuid;
                    if (rawId instanceof UUID u) {
                        mIdUuid = u;
                    } else if (rawId != null && !String.valueOf(rawId).isBlank() && !String.valueOf(rawId).equals("1")) {
                        try {
                            mIdUuid = UUID.fromString(String.valueOf(rawId));
                        } catch (Exception ignored) {
                            mIdUuid = UUID.randomUUID();
                        }
                    } else {
                        mIdUuid = UUID.randomUUID();
                    }

                    String mNome = mDoc.getString("nome");
                    Boolean crianca = Boolean.TRUE.equals(mDoc.get("criancaAte6Anos"));
                    Boolean rsvp = mDoc.get("confirmadoRsvp") != null ? Boolean.TRUE.equals(mDoc.get("confirmadoRsvp")) : null;
                    Boolean checkin = Boolean.TRUE.equals(mDoc.get("presenteCheckin"));
                    membros.add(br.com.convite.domain.MembroConvite.builder()
                            .id(mIdUuid)
                            .nome(mNome != null ? mNome : "")
                            .criancaAte6Anos(crianca)
                            .confirmadoRsvp(rsvp)
                            .presenteCheckin(checkin)
                            .papel(mDoc.getString("papel"))
                            .par(mDoc.getString("par"))
                            .participaCortejo(Boolean.TRUE.equals(mDoc.get("participaCortejo")))
                            .build());
                }
            }
        }

        return Convite.builder()
                .id(id)
                .codigo(codigo)
                .familia(familia)
                .telefone(telefone)
                .email(email)
                .status(status)
                .observacao(observacao)
                .membros(membros)
                .build();
    }
}
