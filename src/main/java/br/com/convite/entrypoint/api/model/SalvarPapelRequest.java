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
public class SalvarPapelRequest {
    private String id;

    @NotBlank(message = "O nome do papel é obrigatório.")
    private String nome;

    private Boolean cortejo;
}
