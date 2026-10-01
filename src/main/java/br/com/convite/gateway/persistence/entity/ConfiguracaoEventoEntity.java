package br.com.convite.gateway.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "configuracao_evento")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoEventoEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String chave; // Identificador lógico único (ex: "principal")

    private LocalDateTime prazoRsvp;
    private LocalDateTime updatedAt;
}
