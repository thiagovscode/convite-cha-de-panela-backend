package br.com.convite.entrypoint.api;

import br.com.convite.domain.AcompanhanteCasamento;
import br.com.convite.entrypoint.api.model.AcompanhanteRequest;
import br.com.convite.entrypoint.api.model.RsvpCasamentoRequest;
import br.com.convite.entrypoint.api.model.RsvpCasamentoResumoResponse;
import br.com.convite.usecase.ProcessarConfirmacaoRsvpCasamentoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rsvp")
@RequiredArgsConstructor
public class RsvpCasamentoController {

    private final ProcessarConfirmacaoRsvpCasamentoUseCase processarConfirmacaoRsvpCasamentoUseCase;

    @PostMapping("/casamento")
    public ResponseEntity<Map<String, Object>> confirmar(@Valid @RequestBody RsvpCasamentoRequest request) {
        List<AcompanhanteCasamento> acompanhantes = null;
        if (request.getAcompanhantes() != null) {
            acompanhantes = request.getAcompanhantes().stream()
                    .map(this::toAcompanhante)
                    .collect(Collectors.toList());
        }

        var resultado = processarConfirmacaoRsvpCasamentoUseCase.executar(
                request.getCodigoConvite(),
                request.getNome(),
                request.getTelefone(),
                request.getEmail(),
                request.getPresenca(),
                acompanhantes,
                request.getObservacao()
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", resultado.message());

        if (resultado.presenca()) {
            response.put("resumo", RsvpCasamentoResumoResponse.builder()
                    .totalPessoas(resultado.totalPessoas())
                    .adultos(resultado.adultos())
                    .criancasAte6Anos(resultado.criancasAte6Anos())
                    .build());
        }

        return ResponseEntity.ok(response);
    }

    private AcompanhanteCasamento toAcompanhante(AcompanhanteRequest req) {
        return AcompanhanteCasamento.builder()
                .id(req.getId())
                .nome(req.getNome() != null ? req.getNome().trim() : null)
                .criancaAte6Anos(req.getCriancaAte6Anos())
                .build();
    }
}