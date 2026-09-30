package br.com.convite.entrypoint.api;

import br.com.convite.usecase.ListarRsvpAdminCasamentoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rsvp/casamento")
@RequiredArgsConstructor
public class AdminRsvpCasamentoController {

    private final ListarRsvpAdminCasamentoUseCase listarRsvpAdminCasamentoUseCase;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        var relatorio = listarRsvpAdminCasamentoUseCase.executar();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("resumoGeral", relatorio.resumoGeral());
        response.put("data", relatorio.data());
        response.put("rsvps", relatorio.data()); // compatibilidade dupla

        return ResponseEntity.ok(response);
    }
}