package br.com.convite.entrypoint.api;

import br.com.convite.entrypoint.api.model.ConfiguracaoEventoResponse;
import br.com.convite.usecase.BuscarConfiguracaoEventoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuracao-evento")
@RequiredArgsConstructor
public class ConfiguracaoEventoPublicaController {

    private final BuscarConfiguracaoEventoUseCase buscarConfiguracaoEventoUseCase;

    @GetMapping
    public ResponseEntity<ConfiguracaoEventoResponse> obterConfiguracaoPublica() {
        return ResponseEntity.ok(buscarConfiguracaoEventoUseCase.executar());
    }
}
