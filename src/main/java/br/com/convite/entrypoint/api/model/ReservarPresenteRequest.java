package br.com.convite.entrypoint.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReservarPresenteRequest {
    @NotBlank(message = "O nome do convidado é obrigatório")
    private String guestName;
}
