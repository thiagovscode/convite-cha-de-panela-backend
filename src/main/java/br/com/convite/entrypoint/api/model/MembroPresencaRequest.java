package br.com.convite.entrypoint.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroPresencaRequest {
    private String membroId;
    private Boolean presente; // true = compareceu, false = não veio (no-show)
}
