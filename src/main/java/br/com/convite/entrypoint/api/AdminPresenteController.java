package br.com.convite.entrypoint.api;

import br.com.convite.domain.Presente;
import br.com.convite.domain.Reserva;
import br.com.convite.gateway.ReservaGateway;
import br.com.convite.usecase.AtualizarQuantidadeMaximaUseCase;
import br.com.convite.usecase.CancelarReservaUseCase;
import br.com.convite.usecase.ListarPresentesUseCase;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/presentes")
@RequiredArgsConstructor
public class AdminPresenteController {

    private final AtualizarQuantidadeMaximaUseCase atualizarQuantidadeMaximaUseCase;
    private final CancelarReservaUseCase cancelarReservaUseCase;
    private final ListarPresentesUseCase listarPresentesUseCase;
    private final ReservaGateway reservaGateway;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listarParaAdmin() {
        List<Presente> presentes = listarPresentesUseCase.executar();
        
        List<AdminPresenteResponse> responseList = presentes.stream().map(p -> {
            List<Reserva> reservas = reservaGateway.buscarPorPresenteId(p.getId());
            List<ReservedByResponse> reservedBy = reservas.stream()
                .filter(r -> "RESERVED".equals(r.getStatus()))
                .map(r -> new ReservedByResponse(r.getGuestName(), r.getId()))
                .collect(Collectors.toList());
                
            return AdminPresenteResponse.builder()
                .id(p.getId())
                .nome(p.getNome())
                .categoria(p.getCategoria())
                .maxQuantity(p.getMaxQuantity())
                .activeReservationsCount(p.getActiveReservationsCount())
                .esgotado(p.getEsgotado())
                .reservedBy(reservedBy)
                .build();
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", responseList);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/quantidade")
    public ResponseEntity<Map<String, Object>> atualizarQuantidade(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        atualizarQuantidadeMaximaUseCase.executar(id, body.get("maxQuantity"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/reservas/{reservaId}")
    public ResponseEntity<Map<String, Object>> cancelarReserva(@PathVariable String reservaId) {
        cancelarReservaUseCase.executar(reservaId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @Data
    @Builder
    public static class AdminPresenteResponse {
        private Long id;
        private String nome;
        private String categoria;
        private Integer maxQuantity;
        private Integer activeReservationsCount;
        private Boolean esgotado;
        private List<ReservedByResponse> reservedBy;
    }

    @Data
    @Builder
    public static class ReservedByResponse {
        private String name;
        private String reservationId;
        
        public ReservedByResponse(String name, String reservationId) {
            this.name = name;
            this.reservationId = reservationId;
        }
    }
}
