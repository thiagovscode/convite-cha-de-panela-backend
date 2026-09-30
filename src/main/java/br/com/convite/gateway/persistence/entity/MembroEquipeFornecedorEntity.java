package br.com.convite.gateway.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroEquipeFornecedorEntity {
    private String id;
    private String nome;
    private String funcao; // Maestro, Violino, Fotógrafo Principal, Assistente, etc.
    private Boolean presente;
    private LocalDateTime dataHoraEntrada;
}
