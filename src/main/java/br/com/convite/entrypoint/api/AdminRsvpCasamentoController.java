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
    public ResponseEntity<Map<String, Object>> listar(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer page,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer size) {

        var relatorio = listarRsvpAdminCasamentoUseCase.executar();
        var listaCompleta = relatorio.data();
        int total = listaCompleta != null ? listaCompleta.size() : 0;

        var listaRetornada = listaCompleta;
        if (page != null && size != null && size > 0 && page >= 0 && listaCompleta != null) {
            int fromIndex = Math.min(page * size, total);
            int toIndex = Math.min(fromIndex + size, total);
            listaRetornada = listaCompleta.subList(fromIndex, toIndex);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("total", total);
        if (page != null && size != null) {
            response.put("page", page);
            response.put("size", size);
        }
        response.put("resumoGeral", relatorio.resumoGeral());
        response.put("data", listaRetornada);
        response.put("rsvps", listaRetornada); // compatibilidade dupla

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(response);
    }
}