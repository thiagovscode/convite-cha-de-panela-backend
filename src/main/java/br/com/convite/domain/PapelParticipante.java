package br.com.convite.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PapelParticipante {
    private String id;
    private String nome;
    private Boolean cortejo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
