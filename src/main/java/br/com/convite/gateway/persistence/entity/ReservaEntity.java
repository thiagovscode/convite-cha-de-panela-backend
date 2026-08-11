package br.com.convite.gateway.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservas")
public class ReservaEntity {
    @Id
    private String id;
    private Long presenteId;
    private String guestName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
