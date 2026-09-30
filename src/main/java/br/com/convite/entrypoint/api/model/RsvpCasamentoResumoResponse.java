package br.com.convite.entrypoint.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RsvpCasamentoResumoResponse {
    private int totalPessoas;
    private long adultos;
    private long criancasAte6Anos;
}