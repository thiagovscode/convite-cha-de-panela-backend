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
public class RsvpCasamento {
    private String id;
    private String nome;
    private String telefone;
    private String email;
    private Boolean presenca;
    private List<AcompanhanteCasamento> acompanhantes;
    private String observacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}