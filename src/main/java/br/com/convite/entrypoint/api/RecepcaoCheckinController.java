package br.com.convite.entrypoint.api;

import br.com.convite.domain.Convite;
import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.domain.ParticipanteCerimonia;
import br.com.convite.entrypoint.api.model.*;
import br.com.convite.usecase.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recepcao")
@RequiredArgsConstructor
public class RecepcaoCheckinController {

    private final BuscarConvitePorCodigoUseCase buscarConvitePorCodigoUseCase;
    private final BuscarConvitesPorTermoUseCase buscarConvitesPorTermoUseCase;
    private final RegistrarCheckinConvidadoUseCase registrarCheckinConvidadoUseCase;
    private final GerarRelatorioAuditoriaUseCase gerarRelatorioAuditoriaUseCase;
    private final ListarParticipantesCerimoniaUseCase listarParticipantesCerimoniaUseCase;
    private final CheckinParticipanteCerimoniaUseCase checkinParticipanteCerimoniaUseCase;
    private final ListarFornecedoresUseCase listarFornecedoresUseCase;
    private final CheckinMembroFornecedorUseCase checkinMembroFornecedorUseCase;
    private final AdicionarMembroFornecedorUseCase adicionarMembroFornecedorUseCase;
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @jakarta.validation.Valid @RequestBody RecepcaoLoginRequest creds,
            jakarta.servlet.http.HttpServletRequest servletRequest) {
        try {
            String userAgent = servletRequest != null ? servletRequest.getHeader("User-Agent") : "";
            LoginResponse authResponse = autenticarUsuarioUseCase.autenticarCompleto(
                    creds.getUsername().trim(), creds.getPassword(), userAgent);

            String userRole = authResponse.getRole() != null ? authResponse.getRole().toUpperCase() : "";
            if (!userRole.contains("RECEPCAO") && !userRole.contains("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "message", "Acesso restrito à equipe de recepção ou administradores."
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", authResponse.getToken(),
                    "accessToken", authResponse.getAccessToken(),
                    "refreshToken", authResponse.getRefreshToken(),
                    "tokenType", authResponse.getTokenType(),
                    "expiresIn", authResponse.getExpiresIn(),
                    "username", authResponse.getUsername(),
                    "role", authResponse.getRole()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Usuário ou senha da recepção incorretos."
            ));
        }
    }

    @GetMapping("/validar-sessao")
    public ResponseEntity<?> validarSessao(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "authenticated", false));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "authenticated", true,
                "username", authentication.getName()
        ));
    }

    @GetMapping("/convite/{codigo}")
    public ResponseEntity<?> buscarConvite(@PathVariable String codigo) {
        Optional<Convite> opt = buscarConvitePorCodigoUseCase.executar(codigo);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Convite não localizado"));
        }
        return ResponseEntity.ok(toFamiliaAuditoria(opt.get()));
    }

    @GetMapping("/busca")
    public ResponseEntity<?> buscarPorTermo(@RequestParam String termo) {
        List<Convite> lista = buscarConvitesPorTermoUseCase.executar(termo);
        List<ItemAuditoriaFamiliaResponse> resp = lista.stream().map(this::toFamiliaAuditoria).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> registrarCheckin(@Valid @RequestBody CheckinRequest request) {
        var presencas = (request.getPresencas() != null)
                ? request.getPresencas().stream()
                        .map(p -> new RegistrarCheckinConvidadoUseCase.PresencaMembro(p.getMembroId(), p.getPresente()))
                        .toList()
                : List.<RegistrarCheckinConvidadoUseCase.PresencaMembro>of();

        var comando = new RegistrarCheckinConvidadoUseCase.Comando(
                request.getCodigo(),
                request.getRecepcionista(),
                presencas
        );
        var resultado = registrarCheckinConvidadoUseCase.executar(comando);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Check-in registrado com sucesso!");
        resp.put("presentes", resultado.presentes());
        resp.put("ausentes", resultado.ausentes());
        resp.put("convite", toFamiliaAuditoria(resultado.convite()));

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/auditoria")
    public ResponseEntity<RelatorioAuditoriaResponse> relatorioAuditoria() {
        var rel = gerarRelatorioAuditoriaUseCase.executar();
        var familias = (rel.convites() != null)
                ? rel.convites().stream().map(this::toFamiliaAuditoria).collect(Collectors.toList())
                : List.<ItemAuditoriaFamiliaResponse>of();

        return ResponseEntity.ok(RelatorioAuditoriaResponse.builder()
                .totalConvidadosPrevistos(rel.totalConvidadosPrevistos())
                .totalAdultosPrevistos(rel.totalAdultosPrevistos())
                .totalCriancasPrevistas(rel.totalCriancasPrevistas())
                .totalConfirmadosRsvp(rel.totalConfirmadosRsvp())
                .totalAdultosConfirmados(rel.totalAdultosConfirmados())
                .totalCriancasConfirmadas(rel.totalCriancasConfirmadas())
                .totalPresentesReais(rel.totalPresentesReais())
                .totalAdultosPresentes(rel.totalAdultosPresentes())
                .totalCriancasPresentes(rel.totalCriancasPresentes())
                .totalAusentesNoShow(rel.totalAusentesNoShow())
                .totalAguardandoChegada(rel.totalAguardandoChegada())
                .totalRecusados(rel.totalRecusados())
                .familias(familias)
                .build());
    }

    @GetMapping("/participantes")
    public ResponseEntity<?> listarParticipantes() {
        List<ParticipanteCerimonia> todos = listarParticipantesCerimoniaUseCase.executar();
        long total = todos.size();
        long confirmadosRsvp = todos.stream().filter(p -> Boolean.TRUE.equals(p.getConfirmadoRsvp())).count();
        long presentes = todos.stream().filter(p -> Boolean.TRUE.equals(p.getPresenteCheckin())).count();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("total", total);
        resp.put("confirmadosRsvp", confirmadosRsvp);
        resp.put("presentes", presentes);
        resp.put("participantes", todos);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/participantes/{id}/checkin")
    public ResponseEntity<?> checkinParticipante(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        Boolean presente = null;
        String statusCortejo = null;
        if (body != null) {
            if (body.get("presente") instanceof Boolean b) {
                presente = b;
            }
            if (body.get("statusCortejo") instanceof String s) {
                statusCortejo = s;
            }
        }
        ParticipanteCerimonia p = checkinParticipanteCerimoniaUseCase.executar(id, presente, statusCortejo);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Status do participante atualizado com sucesso",
                "participante", p
        ));
    }

    @GetMapping("/fornecedores")
    public ResponseEntity<?> listarFornecedores() {
        List<Fornecedor> todos = listarFornecedoresUseCase.executar();
        long totalEmpresas = todos.size();
        long totalMembrosEquipe = 0;
        long totalMembrosPresentes = 0;

        for (Fornecedor f : todos) {
            if (f.getEquipe() != null) {
                totalMembrosEquipe += f.getEquipe().size();
                totalMembrosPresentes += f.getEquipe().stream().filter(m -> Boolean.TRUE.equals(m.getPresente())).count();
            }
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("totalEmpresas", totalEmpresas);
        resp.put("totalMembrosEquipe", totalMembrosEquipe);
        resp.put("totalMembrosPresentes", totalMembrosPresentes);
        resp.put("fornecedores", todos);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/fornecedores/{fornecedorId}/membros/{membroId}/checkin")
    public ResponseEntity<?> checkinMembroFornecedor(
            @PathVariable String fornecedorId,
            @PathVariable String membroId,
            @RequestBody(required = false) Map<String, Boolean> body) {
        Boolean presente = (body != null && body.containsKey("presente")) ? body.get("presente") : null;
        var res = checkinMembroFornecedorUseCase.executar(fornecedorId, membroId, presente);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Presença de " + res.membro().getNome() + " atualizada",
                "membro", res.membro(),
                "fornecedor", res.fornecedor()
        ));
    }

    @PostMapping("/fornecedores/{fornecedorId}/membros")
    public ResponseEntity<?> adicionarMembroEquipe(
            @PathVariable String fornecedorId,
            @RequestBody MembroEquipeFornecedor novoMembro) {
        Fornecedor salvo = adicionarMembroFornecedorUseCase.executar(fornecedorId, novoMembro);
        // Retorna o membro do objeto salvo (já com ID gerado) em vez do objeto do request
        MembroEquipeFornecedor membroSalvo = salvo.getEquipe() != null && !salvo.getEquipe().isEmpty()
                ? salvo.getEquipe().get(salvo.getEquipe().size() - 1)
                : novoMembro;
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Membro adicionado à equipe",
                "membro", membroSalvo,
                "fornecedor", salvo
        ));
    }

    private ItemAuditoriaFamiliaResponse toFamiliaAuditoria(Convite c) {
        List<ItemAuditoriaMembroResponse> membrosResp = (c.getMembros() != null)
                ? c.getMembros().stream().map(this::toMembroAuditoria).collect(Collectors.toList())
                : List.of();

        long confirmados = (c.getMembros() != null) ? c.getMembros().stream().filter(m -> Boolean.TRUE.equals(m.getConfirmadoRsvp())).count() : 0;
        long presentes = (c.getMembros() != null) ? c.getMembros().stream().filter(m -> Boolean.TRUE.equals(m.getPresenteCheckin())).count() : 0;
        long ausentes = (c.getMembros() != null) ? c.getMembros().stream().filter(m -> Boolean.FALSE.equals(m.getPresenteCheckin())).count() : 0;

        return ItemAuditoriaFamiliaResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .familia(c.getFamilia())
                .statusRsvp(c.getStatus() != null ? c.getStatus() : "PENDENTE")
                .telefone(c.getTelefone())
                .papel(c.getPapel())
                .totalMembros(c.getMembros() != null ? c.getMembros().size() : 0)
                .confirmadosRsvp((int) confirmados)
                .presentesCheckin((int) presentes)
                .ausentesNoShow((int) ausentes)
                .membros(membrosResp)
                .build();
    }

    private ItemAuditoriaMembroResponse toMembroAuditoria(MembroConvite m) {
        return ItemAuditoriaMembroResponse.builder()
                .id(m.getId())
                .nome(m.getNome())
                .criancaAte6Anos(Boolean.TRUE.equals(m.getCriancaAte6Anos()))
                .confirmadoRsvp(m.getConfirmadoRsvp())
                .presenteCheckin(m.getPresenteCheckin())
                .dataHoraCheckin(m.getDataHoraCheckin())
                .recepcionista(m.getRecepcionista())
                .papel(m.getPapel())
                .vinculo(m.getVinculo())
                .participaCortejo(m.getParticipaCortejo())
                .build();
    }
}
