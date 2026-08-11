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
@Document(collection = "rsvps")
public class RsvpEntity {
    @Id
    private String id;
    private String name;
    private Boolean confirmed;
    private String message;
    private LocalDateTime createdAt;
}
