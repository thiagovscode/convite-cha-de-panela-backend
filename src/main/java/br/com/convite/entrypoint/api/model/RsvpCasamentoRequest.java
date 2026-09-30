package br.com.convite.entrypoint.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RsvpCasamentoRequest {

    @NotBlank(message = "O nome e obrigatorio.")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
    private String nome;

    @NotBlank(message = "O telefone e obrigatorio.")
    private String telefone;

    @Email(message = "O e-mail informado nao e valido.")
    @Size(max = 255)
    private String email;

    @NotNull(message = "A confirmacao de presenca e obrigatoria.")
    private Boolean presenca;

    @Valid
    private List<AcompanhanteRequest> acompanhantes;

    @Size(max = 500, message = "A observacao nao pode ter mais de 500 caracteres.")
    private String observacao;

    private String codigoConvite;
}