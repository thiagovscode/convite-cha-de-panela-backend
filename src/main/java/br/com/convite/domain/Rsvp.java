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
public class Rsvp {
    private String id;
    private String name;
    private Boolean confirmed;
    private String message;
    private LocalDateTime createdAt;
}
