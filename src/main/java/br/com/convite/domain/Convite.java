package br.com.convite.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Convite {
    private String id;
    private String codigo;
    private String familia;
    private String telefone;
    private String email;
    private List<MembroConvite> membros;
    private String status; // PENDENTE, CONFIRMADO, RECUSADO
    private String observacao;
    private String papel;
    private LocalDateTime dataConfirmacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
