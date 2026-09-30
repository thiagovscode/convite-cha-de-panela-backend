package br.com.convite.entrypoint.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalvarConviteAdminRequest {
    // Se não fornecido, o backend gera automaticamente um código único e seguro
    private String codigo;

    @NotBlank(message = "O nome da família ou convidado principal é obrigatório.")
    private String familia;

    private String telefone;
    private String email;
    private String papel; // Padrinhos, Pais dos Noivos, Família, etc.

    @NotEmpty(message = "Pelo menos um membro deve ser cadastrado.")
    @Valid
    private List<MembroAdminRequest> membros;

    private String observacao;
}
