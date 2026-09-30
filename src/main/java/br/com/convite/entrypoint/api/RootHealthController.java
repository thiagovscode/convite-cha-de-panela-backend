package br.com.convite.entrypoint.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootHealthController {

    @GetMapping({"/", "/health"})
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "convite-backend",
                "message", "Backend Convite Casamento ativo e operante"
        ));
    }
}
