package br.com.convite.entrypoint.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RootHealthController {

    private final MongoTemplate mongoTemplate;

    @GetMapping({"/", "/health"})
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("application", "convite-backend");
        resp.put("version", "2.0.0");

        try {
            Document pingResult = mongoTemplate.executeCommand(new Document("ping", 1));
            Double okVal = pingResult.getDouble("ok");
            Number numVal = pingResult.get("ok", Number.class);
            boolean mongoUp = (numVal != null && numVal.intValue() == 1) || (okVal != null && okVal == 1.0);

            resp.put("status", mongoUp ? "UP" : "DEGRADED");
            resp.put("database", mongoUp ? "CONNECTED" : "UNHEALTHY");
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            log.error("Falha ao verificar saúde do MongoDB no health check: {}", ex.getMessage());
            resp.put("status", "DOWN");
            resp.put("database", "DISCONNECTED");
            resp.put("error", "Banco de dados indisponível");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(resp);
        }
    }
}
