package br.com.convite.gateway.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rsvps")
public class RsvpCasamentoEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String telefone;

    private String nome;
    private String email;
    private Boolean presenca;
    private List<AcompanhanteCasamentoEntity> acompanhantes;
    private String observacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}