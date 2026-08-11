package br.com.convite.entrypoint.api;

import br.com.convite.domain.Rsvp;
import br.com.convite.entrypoint.api.model.RsvpRequest;
import br.com.convite.usecase.ConfirmarRsvpUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rsvp")
@RequiredArgsConstructor
public class RsvpController {

    private final ConfirmarRsvpUseCase confirmarRsvpUseCase;

    @PostMapping
    public ResponseEntity<Map<String, Object>> confirmar(@Valid @RequestBody RsvpRequest request) {
        Rsvp rsvp = confirmarRsvpUseCase.executar(request.getName(), request.getConfirmed(), request.getMessage());
        return ResponseEntity.ok(Map.of("success", true, "data", rsvp));
    }
}
