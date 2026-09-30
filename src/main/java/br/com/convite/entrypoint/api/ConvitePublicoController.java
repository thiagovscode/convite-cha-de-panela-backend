package br.com.convite.entrypoint.api;

import br.com.convite.domain.Convite;
import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.entrypoint.api.model.ConvitePublicoResponse;
import br.com.convite.entrypoint.api.model.MembroPublicoResponse;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.FornecedorNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.usecase.AdicionarMembroFornecedorUseCase;
import br.com.convite.usecase.BuscarConvitePorCodigoUseCase;
import br.com.convite.usecase.BuscarFornecedorPorIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{codigo}")
    public ResponseEntity<?> buscarPorCodigo(@PathVariable String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new RegraDeNegocioException("Código do convite não pode ser vazio.");
        }

        Convite convite = buscarConvitePorCodigoUseCase.executar(codigo.trim())
                .orElseThrow(() -> new ConviteNaoEncontradoException(codigo));

        ConvitePublicoResponse response = ConvitePublicoResponse.builder()
                .codigo(convite.getCodigo())
                .familia(convite.getFamilia())
                .telefone(convite.getTelefone())
                .status(convite.getStatus() != null ? convite.getStatus() : "PENDENTE")
                .membros(convite.getMembros() != null ? convite.getMembros().stream()
                        .map(m -> MembroPublicoResponse.builder()
                                .id(m.getId())
                                .nome(m.getNome())
                                .criancaAte6Anos(Boolean.TRUE.equals(m.getCriancaAte6Anos()))
                                .confirmadoRsvp(m.getConfirmadoRsvp())
                                .build())
                        .collect(Collectors.toList()) : null)
                .build();

        return ResponseEntity.ok(response);
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
}
