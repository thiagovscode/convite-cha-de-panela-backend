package br.com.convite.entrypoint.api;

import br.com.convite.domain.PapelParticipante;
import br.com.convite.domain.VinculoParticipante;
import br.com.convite.entrypoint.api.model.ClassificacoesResponse;
import br.com.convite.entrypoint.api.model.SalvarPapelRequest;
import br.com.convite.usecase.ExcluirPapelUseCase;
import br.com.convite.usecase.ListarPapeisUseCase;
import br.com.convite.usecase.ListarVinculosUseCase;
import br.com.convite.usecase.SalvarPapelUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/configuracoes")
@RequiredArgsConstructor
public class AdminConfiguracaoClassificacaoController {

    private final ListarPapeisUseCase listarPapeisUseCase;
    private final SalvarPapelUseCase salvarPapelUseCase;
    private final ExcluirPapelUseCase excluirPapelUseCase;
    private final ListarVinculosUseCase listarVinculosUseCase;

    @GetMapping("/classificacoes")
    public ResponseEntity<ClassificacoesResponse> obterClassificacoes() {
        return ResponseEntity.ok(ClassificacoesResponse.builder()
                .papeis(listarPapeisUseCase.executar())
                .vinculos(listarVinculosUseCase.executar())
                .build());
    }

    // --- PAPÉIS ---
    @GetMapping("/papeis")
    public ResponseEntity<List<PapelParticipante>> listarPapeis() {
        return ResponseEntity.ok(listarPapeisUseCase.executar());
    }

    @PostMapping("/papeis")
    public ResponseEntity<?> criarPapel(@Valid @RequestBody SalvarPapelRequest req) {
        PapelParticipante papel = PapelParticipante.builder()
                .nome(req.getNome())
                .cortejo(Boolean.TRUE.equals(req.getCortejo()))
                .build();
        PapelParticipante salvo = salvarPapelUseCase.executar(papel);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Papel cadastrado com sucesso!");
        resp.put("papel", salvo);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/papeis/{id}")
    public ResponseEntity<?> atualizarPapel(@PathVariable String id, @Valid @RequestBody SalvarPapelRequest req) {
        PapelParticipante papel = PapelParticipante.builder()
                .id(id)
                .nome(req.getNome())
                .cortejo(Boolean.TRUE.equals(req.getCortejo()))
                .build();
        PapelParticipante salvo = salvarPapelUseCase.executar(papel);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Papel atualizado com sucesso!");
        resp.put("papel", salvo);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/papeis/{id}")
    public ResponseEntity<?> excluirPapel(@PathVariable String id) {
        excluirPapelUseCase.executar(id);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Papel removido com sucesso!");
        return ResponseEntity.ok(resp);
    }
}
