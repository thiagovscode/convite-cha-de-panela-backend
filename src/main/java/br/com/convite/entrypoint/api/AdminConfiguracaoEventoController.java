package br.com.convite.entrypoint.api;

import br.com.convite.entrypoint.api.model.ConfiguracaoEventoRequest;
import br.com.convite.entrypoint.api.model.ConfiguracaoEventoResponse;
import br.com.convite.usecase.AtualizarPrazoRsvpUseCase;
import br.com.convite.usecase.BuscarConfiguracaoEventoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/configuracao-evento")
@RequiredArgsConstructor
public class AdminConfiguracaoEventoController {

    private final BuscarConfiguracaoEventoUseCase buscarConfiguracaoEventoUseCase;
    private final AtualizarPrazoRsvpUseCase atualizarPrazoRsvpUseCase;

    @GetMapping
    public ResponseEntity<ConfiguracaoEventoResponse> obterConfiguracao() {
        return ResponseEntity.ok(buscarConfiguracaoEventoUseCase.executar());
    }

    @PutMapping
    public ResponseEntity<ConfiguracaoEventoResponse> atualizarConfiguracao(@Valid @RequestBody ConfiguracaoEventoRequest req) {
        return ResponseEntity.ok(atualizarPrazoRsvpUseCase.executar(req.getPrazoRsvp()));
    }
}
