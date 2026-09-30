package br.com.convite.entrypoint.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcompanhanteRequest {

    private String id;

    @NotBlank(message = "O nome do acompanhante e obrigatorio.")
    @Size(min = 2, max = 150, message = "O nome do acompanhante deve ter entre 2 e 150 caracteres.")
    private String nome;

    /**
     * Checkbox no convite: Criança até 6 anos
     * true = Criança até 6 anos (isenta)
     * false / null = Adulto ou criança de 7 anos ou mais (pagante)
     */
    private Boolean criancaAte6Anos;
}