package br.com.convite.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fornecedor {
    private String id;
    private String nome;
    private String responsavel;
    private String papel;
    private String categoria;
    private String servico;
    private String empresa;
    private String telefone;
    private String horarioPrevisto;
    private String instrucaoChegada;
    private Boolean chegadaAntecipada;
    private List<MembroEquipeFornecedor> equipe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
