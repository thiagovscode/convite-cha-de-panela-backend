package br.com.convite.entrypoint.api.model;

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
public class CheckinRequest {
    @NotBlank(message = "O codigo do convite e obrigatorio.")
    private String codigo;

    @NotEmpty(message = "A lista de presencas individuais e obrigatoria.")
    private List<MembroPresencaRequest> presencas;

    private String recepcionista;
}
