package br.com.convite.entrypoint.api;

import br.com.convite.domain.Presente;
import br.com.convite.entrypoint.api.model.PresenteResponse;
import br.com.convite.entrypoint.api.model.ReservarPresenteRequest;
import br.com.convite.usecase.ListarPresentesUseCase;
import br.com.convite.usecase.ReservarPresenteUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/presentes")
@RequiredArgsConstructor
public class PresenteController {

    private final ListarPresentesUseCase listarPresentesUseCase;
    private final ReservarPresenteUseCase reservarPresenteUseCase;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<PresenteResponse> responseList = listarPresentesUseCase.executar().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", responseList));
    }

    @PostMapping("/{id}/reservar")
    public ResponseEntity<Map<String, Object>> reservar(@PathVariable Long id, @Valid @RequestBody ReservarPresenteRequest request) {
        reservarPresenteUseCase.executar(id, request.getGuestName());
        return ResponseEntity.ok(Map.of("success", true));
    }

    private PresenteResponse toResponse(Presente presente) {
        return PresenteResponse.builder()
                .id(presente.getId())
                .nome(presente.getNome())
                .imagem(presente.getImagem())
                .categoria(presente.getCategoria())
                .cor(presente.getCor())
                .descricao(presente.getDescricao())
                .linkCompra(presente.getLinkCompra())
                .maxQuantity(presente.getMaxQuantity())
                .activeReservationsCount(presente.getActiveReservationsCount())
                .esgotado(presente.getEsgotado())
                .build();
    }
}
