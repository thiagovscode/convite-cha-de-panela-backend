package br.com.convite.entrypoint.api;

import br.com.convite.domain.Convite;
import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.entrypoint.api.model.ConvitePublicoResponse;
import br.com.convite.entrypoint.api.model.MembroPublicoResponse;
import br.com.convite.exception.ConvidadoNaoEncontradoException;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.FornecedorNaoEncontradoException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.AdicionarMembroFornecedorUseCase;
import br.com.convite.usecase.BuscarConvitePorCodigoUseCase;
import br.com.convite.usecase.BuscarFornecedorPorIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/convites")
@RequiredArgsConstructor
public class ConvitePublicoController {

    private final BuscarConvitePorCodigoUseCase buscarConvitePorCodigoUseCase;
    private final BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;
    private final AdicionarMembroFornecedorUseCase adicionarMembroFornecedorUseCase;
    private final ConviteGateway conviteGateway;

    /**
     * Endpoint para busca pública via query params:
     * - ?codigo=...
     * - ?codigo=...&membroId=...
     * - ?id=...
     */
    @GetMapping
    public ResponseEntity<?> buscarPorParametros(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String membroId,
            @RequestParam(required = false) String convidadoId) {

        String conviteIdentificador = (codigo != null && !codigo.isBlank()) ? codigo : id;
        if (conviteIdentificador == null || conviteIdentificador.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código ou ID do convite é obrigatório."));
        }

        String membroIdentificador = (membroId != null && !membroId.isBlank()) ? membroId : convidadoId;
        if (membroIdentificador != null && !membroIdentificador.isBlank()) {
            return buscarConvidadoPorConviteEId(conviteIdentificador, membroIdentificador);
        }

        return buscarConvite(conviteIdentificador, id, codigo);
    }

    /**
     * Caso 1: Busca somente o convite pelo código ou ID.
     */
    @GetMapping("/{codigoOuId}")
    public ResponseEntity<?> buscarConvite(
            @PathVariable String codigoOuId,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String codigo) {

        Optional<Convite> opt;
        if (codigo != null && !codigo.isBlank() && id != null && !id.isBlank()) {
            opt = conviteGateway.buscarPorCodigoEId(codigo, id);
        } else {
            opt = conviteGateway.buscarPorCodigoOuId(codigoOuId);
        }

        Convite convite = opt.orElseThrow(() -> new ConviteNaoEncontradoException(codigoOuId));

        ConvitePublicoResponse response = toPublicResponse(convite);
        return ResponseEntity.ok(response);
    }

    /**
     * Casos 2, 3, 4, 5: Busca assertiva de dois níveis.
     * Nível 1: Localiza o convite pelo CÓDIGO do convite. Se não encontrado -> 404 Convite Não Encontrado.
     * Nível 2: Busca estritamente dentro de convite.membros pelo ID do membro. Se não encontrado -> 404 "Convidado não pertence a este convite".
     */
    @GetMapping({"/{codigoConvite}/membros/{membroId}", "/{codigoConvite}/convidados/{membroId}"})
    public ResponseEntity<?> buscarConvidadoPorConviteEId(
            @PathVariable String codigoConvite,
            @PathVariable String membroId) {

        // 1. PRIMEIRO NÍVEL: Localiza o convite pelo código do convite
        Convite convite = conviteGateway.buscarPorCodigo(codigoConvite.trim())
                .or(() -> conviteGateway.buscarPorCodigoOuId(codigoConvite.trim()))
                .orElseThrow(() -> new ConviteNaoEncontradoException(codigoConvite));

        // 2. SEGUNDO NÍVEL: Busca membro exclusivamente dentro do array de membros do convite pelo ID do membro
        MembroConvite membro = (convite.getMembros() != null ? convite.getMembros().stream() : java.util.stream.Stream.<MembroConvite>empty())
                .filter(m -> m.getId() != null && m.getId().trim().equalsIgnoreCase(membroId.trim()))
                .findFirst()
                .orElseThrow(() -> new ConvidadoNaoEncontradoException("Convidado não pertence a este convite"));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("conviteId", convite.getId());
        resp.put("codigoConvite", convite.getCodigo());
        resp.put("familia", convite.getFamilia());
        resp.put("statusConvite", convite.getStatus());
        resp.put("convidado", MembroPublicoResponse.builder()
                .id(membro.getId())
                .nome(membro.getNome())
                .criancaAte6Anos(Boolean.TRUE.equals(membro.getCriancaAte6Anos()))
                .confirmadoRsvp(membro.getConfirmadoRsvp())
                .build());
        resp.put("convite", toPublicResponse(convite));

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/fornecedor/{id}")
    public ResponseEntity<?> buscarFornecedorPublico(@PathVariable String id) {
        Fornecedor fornecedor = buscarFornecedorPorIdUseCase.executar(id)
                .orElseThrow(() -> new FornecedorNaoEncontradoException(id));
        return ResponseEntity.ok(fornecedor);
    }

    @PostMapping("/fornecedor/{id}/membros")
    public ResponseEntity<?> adicionarMembroPublico(
            @PathVariable String id,
            @RequestBody MembroEquipeFornecedor novoMembro) {
        Fornecedor salvo = adicionarMembroFornecedorUseCase.executar(id, novoMembro);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Membro adicionado com sucesso!",
                "membro", novoMembro,
                "fornecedor", salvo
        ));
    }

    private ConvitePublicoResponse toPublicResponse(Convite convite) {
        return ConvitePublicoResponse.builder()
                .id(convite.getId())
                .codigo(convite.getCodigo())
                .familia(convite.getFamilia())
                .telefone(convite.getTelefone())
                .status(convite.getStatus() != null ? convite.getStatus() : "PENDENTE")
                .papel(convite.getPapel())
                .membros(convite.getMembros() != null ? convite.getMembros().stream()
                        .map(m -> MembroPublicoResponse.builder()
                                .id(m.getId())
                                .nome(m.getNome())
                                .criancaAte6Anos(Boolean.TRUE.equals(m.getCriancaAte6Anos()))
                                .confirmadoRsvp(m.getConfirmadoRsvp())
                                .build())
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
