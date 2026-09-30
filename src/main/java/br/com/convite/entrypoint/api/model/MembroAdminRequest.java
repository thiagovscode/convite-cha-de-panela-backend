package br.com.convite.entrypoint.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroAdminRequest {
    private String id;

    @NotBlank(message = "O nome do membro e obrigatorio.")
    private String nome;

    private Boolean criancaAte6Anos;
    private String papel; // Padrinho, Madrinha, Pajem, Florista, etc.
    private String vinculo; // Noivo, Noiva, etc.
    private String par; // Par do cortejo (ex: Madrinha do Padrinho)
    private Boolean participaCortejo;
}
