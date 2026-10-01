package br.com.convite.entrypoint.api;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.MetricasCasamento;
import br.com.convite.entrypoint.api.model.DashboardMetricasResponse;
import br.com.convite.entrypoint.api.model.MembroAdminRequest;
import br.com.convite.entrypoint.api.model.SalvarConviteAdminRequest;
import br.com.convite.usecase.CalcularMetricasCasamentoUseCase;
import br.com.convite.usecase.ExcluirConviteUseCase;
import br.com.convite.usecase.ListarConvitesUseCase;
import br.com.convite.usecase.SalvarConviteUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/convites")
@RequiredArgsConstructor
public class AdminConviteController {

    private final ListarConvitesUseCase listarConvitesUseCase;
    private final SalvarConviteUseCase salvarConviteUseCase;
    private final br.com.convite.usecase.CriarConviteUseCase criarConviteUseCase;
    private final br.com.convite.usecase.AtualizarConviteUseCase atualizarConviteUseCase;
    private final ExcluirConviteUseCase excluirConviteUseCase;
    private final CalcularMetricasCasamentoUseCase calcularMetricasCasamentoUseCase;
    private final br.com.convite.usecase.DefinirParCortejoUseCase definirParCortejoUseCase;
    private final br.com.convite.usecase.ResetarRsvpConviteUseCase resetarRsvpConviteUseCase;

    @GetMapping
    public ResponseEntity<List<Convite>> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        List<Convite> todos = listarConvitesUseCase.executar();
        int total = todos != null ? todos.size() : 0;

        if (page != null && size != null && size > 0 && page >= 0 && todos != null) {
            int fromIndex = Math.min(page * size, total);
            int toIndex = Math.min(fromIndex + size, total);
            List<Convite> paginados = todos.subList(fromIndex, toIndex);

            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(total))
                    .header("X-Page", String.valueOf(page))
                    .header("X-Size", String.valueOf(size))
                    .body(paginados);
        }

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(todos);
    }

    @PutMapping("/definir-par")
    public ResponseEntity<?> definirPar(@RequestBody Map<String, String> body) {
        String codigoConvite = body.get("codigoConvite");
        String membroId = body.get("membroId");
        String nomeMembro = body.get("nomeMembro");
        String nomePar = body.get("nomePar");

        Convite atualizado = definirParCortejoUseCase.executar(codigoConvite, membroId, nomeMembro, nomePar);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Par do cortejo atualizado com sucesso!",
                "convite", atualizado
        ));
    }

    @PostMapping
    public ResponseEntity<?> salvarNovo(@Valid @RequestBody SalvarConviteAdminRequest req) {
        return processarSalvar(req);
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody SalvarConviteAdminRequest req) {
        return processarSalvar(req);
    }

    @PutMapping("/{codigoOuId}")
    public ResponseEntity<?> atualizarPorParametro(
            @PathVariable String codigoOuId,
            @Valid @RequestBody SalvarConviteAdminRequest req) {
        if (req.getCodigo() == null || req.getCodigo().isBlank()) {
            req.setCodigo(codigoOuId);
        }
        if (req.getId() == null || req.getId().isBlank()) {
            req.setId(codigoOuId);
        }
        return processarAtualizacao(req);
    }

    @PutMapping
    public ResponseEntity<?> atualizar(@Valid @RequestBody SalvarConviteAdminRequest req) {
        return processarAtualizacao(req);
    }

    private ResponseEntity<?> processarSalvar(SalvarConviteAdminRequest req) {
        Convite dados = toConviteDomain(req);
        Convite salvo = salvarConviteUseCase.executar(dados);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Convite salvo com sucesso!");
        resp.put("codigo", salvo.getCodigo());
        resp.put("convite", salvo);

        return ResponseEntity.ok(resp);
    }

    private ResponseEntity<?> processarAtualizacao(SalvarConviteAdminRequest req) {
        Convite dados = toConviteDomain(req);
        Convite atualizado = atualizarConviteUseCase.executar(dados);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Convite atualizado com sucesso!");
        resp.put("codigo", atualizado.getCodigo());
        resp.put("convite", atualizado);

        return ResponseEntity.ok(resp);
    }

    private Convite toConviteDomain(SalvarConviteAdminRequest req) {
        return Convite.builder()
                .id(req.getId())
                .codigo(req.getCodigo())
                .familia(req.getFamilia())
                .telefone(req.getTelefone())
                .email(req.getEmail())
                .observacao(req.getObservacao())
                .membros(req.getMembros() != null ? req.getMembros().stream()
                        .map(this::toMembroDomain)
                        .collect(Collectors.toList()) : null)
                .build();
    }


    /**
     * Reseta o status de um convite para PENDENTE e remove o registro de RSVP associado.
     * Permite que os noivos reenviem o convite para convidados que mudaram de ideia.
     * Toda a regra de negócio fica isolada em ResetarRsvpConviteUseCase.
     */
    @PostMapping("/{codigoOuId}/resetar-rsvp")
    public ResponseEntity<?> resetarRsvp(@PathVariable String codigoOuId) {
        Convite salvo = resetarRsvpConviteUseCase.executar(codigoOuId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "RSVP da família " + salvo.getFamilia() + " resetado com sucesso! O convite agora está Pendente.");
        resp.put("codigo", salvo.getCodigo());
        resp.put("status", salvo.getStatus());
        resp.put("convite", salvo);

        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{codigoOuId}")
    public ResponseEntity<?> excluir(@PathVariable String codigoOuId) {
        Convite excluido = excluirConviteUseCase.executar(codigoOuId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Convite da " + excluido.getFamilia() + " excluído com sucesso!");
        resp.put("codigo", excluido.getCodigo());
        resp.put("familia", excluido.getFamilia());

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/metricas")
    public ResponseEntity<DashboardMetricasResponse> obterMetricas() {
        MetricasCasamento m = calcularMetricasCasamentoUseCase.executar();
        return ResponseEntity.ok(toMetricasResponse(m));
    }

    private DashboardMetricasResponse toMetricasResponse(MetricasCasamento m) {
        return DashboardMetricasResponse.builder()
                .totalConvites(m.getTotalConvites())
                .totalConvitesConfirmados(m.getTotalConvitesConfirmados())
                .totalConvitesRecusados(m.getTotalConvitesRecusados())
                .totalConvitesPendentes(m.getTotalConvitesPendentes())
                .totalPessoas(m.getTotalPessoas())
                .totalConfirmados(m.getTotalConfirmados())
                .totalRecusaram(m.getTotalRecusaram())
                .totalPendentes(m.getTotalPendentes())
                .totalAdultosConfirmados(m.getTotalAdultosConfirmados())
                .totalCriancasConfirmadas(m.getTotalCriancasConfirmadas())
                .taxaConfirmacao(m.getTaxaConfirmacao())
                .taxaRecusa(m.getTaxaRecusa())
                .taxaPendentes(m.getTaxaPendentes())
                .taxaPresencaRespondidos(m.getTaxaPresencaRespondidos())
                .build();
    }

    private MembroConvite toMembroDomain(MembroAdminRequest req) {
        UUID membroId = null;
        if (req.getId() != null && !req.getId().isBlank() && !req.getId().trim().equals("1") && !req.getId().trim().matches("^\\d+$")) {
            try {
                membroId = UUID.fromString(req.getId().trim());
            } catch (Exception ignored) {
                membroId = UUID.nameUUIDFromBytes(req.getId().trim().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        if (membroId == null) {
            membroId = UUID.randomUUID();
        }

        return MembroConvite.builder()
                .id(membroId)
                .nome(req.getNome())
                .criancaAte6Anos(Boolean.TRUE.equals(req.getCriancaAte6Anos()))
                .papel(req.getPapel())
                .par(req.getPar())
                .participaCortejo(req.getParticipaCortejo())
                .build();
    }
}
