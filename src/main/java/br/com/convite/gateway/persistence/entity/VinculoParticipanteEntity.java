package br.com.convite.gateway.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vinculos_participantes")
public class VinculoParticipanteEntity {
    @Id
    private String id;
    private String nome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
