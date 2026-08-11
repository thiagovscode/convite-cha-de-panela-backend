package br.com.convite.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    private String id;
    private Long presenteId;
    private String guestName;
    private String status; // RESERVED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
