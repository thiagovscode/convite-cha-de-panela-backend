package br.com.convite.entrypoint.api;

import br.com.convite.domain.Rsvp;
import br.com.convite.entrypoint.api.model.RsvpResponse;
import br.com.convite.usecase.ListarRsvpUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/rsvp")
@RequiredArgsConstructor
public class AdminRsvpController {

    private final ListarRsvpUseCase listarRsvpUseCase;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<RsvpResponse> responseList = listarRsvpUseCase.executar().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", responseList));
    }

    private RsvpResponse toResponse(Rsvp rsvp) {
        return RsvpResponse.builder()
                .id(rsvp.getId())
                .name(rsvp.getName())
                .confirmed(rsvp.getConfirmed())
                .message(rsvp.getMessage())
                .createdAt(rsvp.getCreatedAt())
                .build();
    }
}
