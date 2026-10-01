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
@Document(collection = "convites")
public class ConviteCasamentoEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String codigo;

    private String familia;
    private String telefone;
    private String email;
    private List<MembroConviteEntity> membros;
    private String status; // PENDENTE, CONFIRMADO, RECUSADO
    private String observacao;
    private LocalDateTime dataConfirmacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
