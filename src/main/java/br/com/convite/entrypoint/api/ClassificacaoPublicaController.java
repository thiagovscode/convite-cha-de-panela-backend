package br.com.convite.entrypoint.api;

import br.com.convite.domain.PapelParticipante;
import br.com.convite.domain.VinculoParticipante;
import br.com.convite.entrypoint.api.model.ClassificacoesResponse;
import br.com.convite.usecase.ListarPapeisUseCase;
import br.com.convite.usecase.ListarVinculosUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classificacoes")
@RequiredArgsConstructor
public class ClassificacaoPublicaController {

    private final ListarPapeisUseCase listarPapeisUseCase;
    private final ListarVinculosUseCase listarVinculosUseCase;

    @GetMapping
    public ResponseEntity<ClassificacoesResponse> obterClassificacoes() {
        return ResponseEntity.ok(ClassificacoesResponse.builder()
                .papeis(listarPapeisUseCase.executar())
                .vinculos(listarVinculosUseCase.executar())
                .build());
    }

    @GetMapping("/papeis")
    public ResponseEntity<List<PapelParticipante>> listarPapeis() {
        return ResponseEntity.ok(listarPapeisUseCase.executar());
    }

    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/vinculos")
    public ResponseEntity<List<VinculoParticipante>> listarVinculos() {
        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .header("Sunset", "Wed, 31 Dec 2026 23:59:59 GMT")
                .body(listarVinculosUseCase.executar());
    }
}

